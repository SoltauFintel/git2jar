package git2jar.build;

import github.soltaufintel.amalia.web.action.Action;

public class CreateJobAction extends Action {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String tag = ctx.pathParam("tag");

		String jobId = new BuildService().createJob(id, tag);
		
		ctx.redirect("/job/" + jobId);
	}
}
