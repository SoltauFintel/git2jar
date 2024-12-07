package git2jar.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pmw.tinylog.Logger;

import git2jar.build.Job.JobStatus;
import git2jar.config.Config;
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
		BuildResult ret = new BuildResult();
		File dir = new File(Config.config.getTagsWorkDir(), job.getJobId());
		dir.mkdirs();
		Logger.info("job dir: " + dir.getAbsolutePath());
		
		// TODO Baustelle
		
		return ret;
	}
	
	public void clearDoneJobs() {
		synchronized (HANDLE) {
			jobs.removeIf(job -> job.getStatus().equals(JobStatus.FINISHED));
		}
	}
}
