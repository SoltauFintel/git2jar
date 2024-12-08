package git2jar.build;

import java.io.File;
import java.io.IOException;

import org.pmw.tinylog.Logger;

import com.google.common.io.Files;

import git2jar.base.Config;
import git2jar.base.FileService;
import git2jar.docker.AbstractDocker;
import git2jar.project.Project;
import git2jar.project.ProjectService;

public class BuildService {
	
	public BuildResult build(Job job) {
		File dir = new File(Config.config.getJobsWorkDir(), job.getJobId());
		dir.mkdirs();
		Logger.info("Building job #" + job.getJobId() + " ... | dir: " + dir.getAbsolutePath());
		String cmd = getCommand(job);
		String image = Config.config.getImage();
		FileService.savePlainTextFile(new File(dir, "SCRIPT"), cmd);
		AbstractDocker docker = AbstractDocker.get();
		long start = System.currentTimeMillis();
		docker.pull(image);
		
		BuildResult ret = docker.run(image, dir, "/work");
		
		long end = System.currentTimeMillis();
		Logger.info("container #" + ret.getId() + " log:\n" + ret.getLog());
		
		// Ergebnisdateien holen
		File output = new File(dir, "output");
		StringBuilder sb = new StringBuilder();
		if (output.isDirectory()) {
			copyOutputFilesToRepository(output, sb);
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

	private void copyOutputFilesToRepository(File dir, StringBuilder sb) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					sb.append(file.getAbsolutePath() + "\n");
					String name = file.getAbsolutePath().replace("\\", "/");
					int o = name.indexOf("repository/");
					name = name.substring(o + "repository/".length());
					File target = new File(Config.config.getRepositoryDir(), name);
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
					copyOutputFilesToRepository(file, sb); // recursive
				}
			}
		}
	}

	private String getCommand(Job job) {
		Project p = job.getProject();
		String lp = p.getLastUrlPart();
		String cmd1 = "git clone -b " + job.getTag() + " " + p.getUrl();
		String cmd2 = p.getBuildCommand().replace("{tag}", job.getTag());
		String cmd = cmd1 + " && cd " + lp + "/" + lp + " && " + cmd2 + " && mkdir /work/output"
				+ " && cp -R ~/.m2/repository /work/output";
		Logger.info("SCRIPT: " + cmd);
		return cmd;
	}
}
