package git2jar.project;

import github.soltaufintel.amalia.web.action.Action;

public class ReloadProjectsAction extends Action {

	@Override
	protected void execute() {
		new ProjectService().clearCache();

		ctx.redirect("/");
	}
}
