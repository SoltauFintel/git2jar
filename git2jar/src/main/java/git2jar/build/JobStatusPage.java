package git2jar.build;

import git2jar.build.Job.JobStatus;
import github.soltaufintel.amalia.web.action.Page;

public class BuildPage extends Page {

	@Override
	protected void execute() {
		String jobId = ctx.pathParam("jobId");

		Job job = new BuildService().getJob(jobId);
		String log = "";
		if (job.getBuildResult() != null) {
			log = esc(job.getBuildResult().getLog());
		}
		
		put("title", "Building " + job.getProject().getLastUrlPart());
		put("jobId", esc(job.getJobId()));
		put("url", esc(job.getProject().getUrl()));
		put("tag", esc(job.getTag()));
		put("status", esc(job.getStatus().name()));
		put("log", log);
		put("hasLog", !log.isBlank());
		put("success", job.getBuildResult() == null ? false : job.getBuildResult().isSuccess());
		put("finished", job.getStatus().equals(JobStatus.FINISHED));
		put("duration", job.getBuildResult() != null && job.getBuildResult().getDuration() > 0 && job.getStatus().equals(JobStatus.FINISHED)
				? (job.getBuildResult().getDuration() + "ms") : "");
	}
}
