package git2jar.job;

import github.soltaufintel.amalia.web.action.Action;

/**
 * You must clear done jobs if you want to start a job (same project, same tag) again.
 */
public class ClearDoneJobsAction extends Action {

	@Override
	protected void execute() {
		new JobService().clearDoneJobs();
		
		ctx.redirect("/project/home");
	}
}
