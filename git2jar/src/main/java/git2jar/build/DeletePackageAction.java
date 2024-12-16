package git2jar.build;

import org.pmw.tinylog.Logger;

import git2jar.project.ProjectService;
import github.soltaufintel.amalia.web.action.Action;

public class DeletePackageAction extends Action {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String tag = ctx.pathParam("tag");
		
		if (new ProjectService().deletePackage(id, tag)) {
			Logger.info("package deleted: #" + id + ", tag " + tag);
		}
		
		ctx.redirect("/project/home");
	}
}
