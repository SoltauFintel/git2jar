package git2jar.job;

import git2jar.job.Job.JobStatus;
import github.soltaufintel.amalia.web.action.Page;

public class JobStatusPage extends Page {

	@Override
	protected void execute() {
		String jobId = ctx.pathParam("jobId");

		Job job = new JobService().getJob(jobId);
		String log = "";
		if (job.getBuildResult() != null) {
			log = esc(job.getBuildResult().getLog());
		}
		
		put("title", esc("Building " + job.getProject().getLastUrlPart() + " " + job.getTag()));
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
