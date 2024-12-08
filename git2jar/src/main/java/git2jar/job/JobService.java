package git2jar.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pmw.tinylog.Logger;

import git2jar.job.Job.JobStatus;
import git2jar.project.Project;
import git2jar.project.ProjectService;

public class JobService {
	private static final String HANDLE = "HANDLE_JS";
	public static final List<Job> jobs = new ArrayList<>();

	/**
	 * @param id project ID
	 * @param tag version
	 * @return Job ID
	 */
	public String createJob(String id, String tag) {
		synchronized (HANDLE) {
	    	Project project = new ProjectService().get(id);
	    	Job job = new Job(project, tag);
	    	String jobId = job.getJobId();
	    	jobs.add(job);
			Logger.info("Job #" + jobId + " created. " + project.getUrl() + ", " + tag);
	    	startNextJob();
			return jobId;
		}
	}
	
	public Job getJob(String jobId) {
		synchronized (HANDLE) {
			return jobs.stream().filter(job -> job.getJobId().equals(jobId)).findFirst().orElse(null);
		}
	}
	
	public static void startNextJob() {
		Optional<Job> next = jobs.stream().filter(j -> j.getStatus() == JobStatus.WAITING).findFirst();
		if (next.isPresent()) {
			// start next job
			next.get().start();
		}
	}
	
	public void clearDoneJobs() {
		synchronized (HANDLE) {
			jobs.removeIf(job -> job.getStatus().equals(JobStatus.FINISHED));
		}
	}
}
