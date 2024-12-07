package git2jar.build;

import org.pmw.tinylog.Logger;

import git2jar.project.Project;

public class Job {
	private final Project project;
	private final String tag;
	private JobStatus status = JobStatus.WAITING;
	private BuildResult buildResult;
	
	public Job(Project project, String tag) {
		this.project = project;
		this.tag = tag;
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
		long start = System.currentTimeMillis();
		try {
			buildResult = new BuildService().build(project, tag);
			System.out.println(":-) success"); // XXX DEBUG
		} finally {
			status = JobStatus.FINISHED;
			Logger.info("Job finished. " + project.getUrl() + ", " + tag + ", " + (System.currentTimeMillis() - start) + "ms");
		}
	}

	public enum JobStatus {
		WAITING,
		RUNNING,
		FINISHED;
	}
}
