package git2jar.project;

import github.soltaufintel.amalia.web.action.Page;

public class AddProjectPage extends Page {

    @Override
    protected void execute() {
        if (isPOST()) {
            new ProjectService().create(ctx.formParam("url"));
            ctx.redirect("/project");
        } else {
            put("title", "Add project");
        }
    }
}
