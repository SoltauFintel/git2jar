package git2jar.job;

import github.soltaufintel.amalia.web.action.Action;

public class CreateJobAction extends Action {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String tag = ctx.pathParam("tag");

		String jobId = new JobService().createJob(id, tag);
		
		ctx.redirect("/job/" + jobId);
	}
}
