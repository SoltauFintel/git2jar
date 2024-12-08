package git2jar.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pmw.tinylog.Logger;

import com.google.common.io.Files;

import git2jar.base.Config;
import git2jar.base.FileService;
import git2jar.base.ShellScriptExecutor;
import git2jar.build.Job.JobStatus;
import git2jar.docker.AbstractDocker;
import git2jar.docker.UnixDocker;
import git2jar.docker.WindowsDocker;
import git2jar.project.Project;
import git2jar.project.ProjectService;

public class BuildService {
	private static final String HANDLE = "HANDLE_BS";
	public static final List<Job> jobs = new ArrayList<>();
    // TODO Build soll in einem extra Container laufen, der nur während des Builds existiert.
    // TODO Ergebnisdateien dem serve-Programm zur Verfügung stellen

	/**
	 * Get job or creates job if it does not exist
	 * @param id project ID
	 * @param tag version
	 * @return Job
	 */
	public Job getStatus(String id, String tag) {
		synchronized (HANDLE) {
			Job ret = jobs.stream().filter(job -> job.getProject().getId().equals(id) && job.getTag().equals(tag)).findFirst().orElse(null);
			if (ret == null) {
				Logger.info("Job #" + id + ", " + tag + " not found -> create new job! " + jobs.size());
				ret = createJob(id, tag);
			}
			return ret;
		}
	}
	
    private Job createJob(String id, String tag) {
    	Project p = new ProjectService().get(id);
    	Job job = new Job(p, tag);
    	jobs.add(job);
    	Logger.info("Job created. " + p.getUrl() + ", " + tag);
    	startNextJob();
		return job;
	}

	public static void startNextJob() {
		Optional<Job> next = BuildService.jobs.stream().filter(j -> j.getStatus() == JobStatus.WAITING).findFirst();
		if (next.isPresent()) {
			// start next job
			next.get().start();
		}
	}

	public BuildResult build(Job job) {
		File dir = new File(Config.config.getJobsWorkDir(), job.getJobId());
		dir.mkdirs();
		Logger.info("Building job #" + job.getJobId() + " ... | dir: " + dir.getAbsolutePath());
		String cmd = getCommand(job);
		String image = Config.config.getImage();
		FileService.savePlainTextFile(new File(dir, "script"), cmd);
		AbstractDocker docker = docker();
		long start = System.currentTimeMillis();
		docker.pull(image);
		
		BuildResult ret = docker.run(image, dir, "/work");
		
		long end = System.currentTimeMillis();
		Logger.info("container #" + ret.getId() + " log:\n" + ret.getLog());
		
		// Ergebnisdateien holen
		File output = new File(dir, "output");
		StringBuilder sb = new StringBuilder();
		if (output.isDirectory()) {
			list(output, sb);
		} else {
			Logger.error("Output dir does not exist: " + output.getAbsolutePath());
		}
		
		docker.rmf(ret.getId());

		new ProjectService().clearCache();
		
		ret.setDuration(end - start);
		ret.setSuccess(ret.getLog() != null && ret.getLog().contains("BUILD SUCCESSFUL"));
		ret.setLog(ret.getLog() + "\nOutput files:\n" + sb.toString());
		return ret;
	}

	private void list(File dir, StringBuilder sb) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					sb.append(file.getAbsolutePath() + "\n");
					String name = file.getAbsolutePath().replace("\\", "/");
					int o = name.indexOf("repository/");
					name = name.substring(o + "repository/".length());
					File target = new File(Config.config.getFilesDir(), name);
					target.getParentFile().mkdirs();
					try {
						Files.copy(file, target);
					} catch (IOException e) {
						Logger.error(e, "copy error: " + file.getAbsolutePath() + " -> " + target.getAbsolutePath()
								+ "\n" + e.getMessage());
					}
				}
			}
			for (File file : files) {
				if (file.isDirectory()) {
					list(file, sb); // recursive
				}
			}
		}
	}

	private String getCommand(Job job) {
		Project p = job.getProject();
		String lp = p.getLastUrlPart();
		String cmd1 = "git clone " + p.getUrl();
		String cmd2 = p.getBuildCommand().replace("{tag}", job.getTag());
		String cmd = cmd1 + " && cd " + lp + "/" + lp + " && " + cmd2 + " && mkdir /work/output"
				+ " && cp -R ~/.m2/repository /work/output";
		Logger.info("SCRIPT: " + cmd);
		return cmd;
	}

	private AbstractDocker docker() {
		AbstractDocker docker;
		if (ShellScriptExecutor.isWindows()) {
			docker = new WindowsDocker();
		} else {
			docker = new UnixDocker();
		}
		return docker;
	}
	
	public void clearDoneJobs() {
		synchronized (HANDLE) {
			jobs.removeIf(job -> job.getStatus().equals(JobStatus.FINISHED));
		}
	}
}
