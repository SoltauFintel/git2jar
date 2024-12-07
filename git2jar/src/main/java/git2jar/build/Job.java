package git2jar.build;

import org.pmw.tinylog.Logger;

import git2jar.project.Project;
import github.soltaufintel.amalia.base.IdGenerator;

public class Job {
	private final String jobId;
	private final Project project;
	private final String tag;
	private JobStatus status = JobStatus.WAITING;
	private BuildResult buildResult;
	
	public Job(Project project, String tag) {
		jobId = IdGenerator.createId6();
		this.project = project;
		this.tag = tag;
	}

	public String getJobId() {
		return jobId;
	}

	public Project getProject() {
		return project;
	}

	public String getTag() {
		return tag;
	}
	
	public JobStatus getStatus() {
		return status;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public BuildResult getBuildResult() {
		return buildResult;
	}

	public void setBuildResult(BuildResult buildResult) {
		this.buildResult = buildResult;
	}

	public void start() {
		new Thread(() -> {
			execute();
			BuildService.startNextJob();
		}).start();
	}
	
	public void execute() {
		status = JobStatus.RUNNING;
		Logger.info("Job running. " + project.getUrl() + ", " + tag);
		int success = -1;
		long start = System.currentTimeMillis();
		try {
			buildResult = new BuildService().build(this);
			success = buildResult.isSuccess() ? 2 : 1;
		} catch (Exception e) {
			Logger.error(e);
			success = 0;
		} finally {
			status = JobStatus.FINISHED;
			Logger.info("Job finished. " + project.getUrl() + ", " + tag + ", " + (System.currentTimeMillis() - start) + "ms, success: " + success
					+ " (2=success, 1=executed but no success, 0=error, -1=not executed");
		}
	}

	public enum JobStatus {
		WAITING,
		RUNNING,
		FINISHED;
	}
}
