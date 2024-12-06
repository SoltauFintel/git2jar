package git2jar.project;

import github.soltaufintel.amalia.web.action.Action;

public class DeleteProjectAction extends Action {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");
        
        new ProjectService().delete(id);
        
        ctx.redirect("/project");
    }
}
