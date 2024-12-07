package git2jar.build;

import git2jar.build.Job.JobStatus;
import github.soltaufintel.amalia.web.action.Page;

public class BuildPage extends Page {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String tag = ctx.pathParam("tag");

		Job job = new BuildService().getStatus(id, tag);
		
		put("title", "Building " + job.getProject().getLastUrlPart());
		put("jobId", esc(job.getJobId()));
		put("url", esc(job.getProject().getUrl()));
		put("tag", esc(job.getTag()));
		put("status", esc(job.getStatus().name()));
		String log = job.getBuildResult() == null ? "" : esc(job.getBuildResult().getLog());
		put("log", log.isEmpty() && !job.getStatus().equals(JobStatus.FINISHED) ? "(no log available yet)" : log);
		put("success", job.getBuildResult() == null ? false : job.getBuildResult().isSuccess());
		put("finished", job.getStatus().equals(JobStatus.FINISHED));
		put("duration", job.getBuildResult().getDuration() > 0 && job.getStatus().equals(JobStatus.FINISHED)
				? (job.getBuildResult().getDuration() + "ms") : "");
	}
}
