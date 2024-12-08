package git2jar.project;

import github.soltaufintel.amalia.web.action.Page;

public class EditProjectPage extends Page {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        ProjectService sv = new ProjectService();
        Project p = sv.get(id);
        if (isPOST()) {
            p.setUrl(ctx.formParam("url"));
            p.setBuildCommand(ctx.formParam("buildCommand"));
            p.setDir(ctx.formParam("dir"));
            p.getUser().setLogin(ctx.formParam("login"));
            p.getUser().setPassword(ctx.formParam("password"));
            p.setBranch(ctx.formParam("branch"));
            sv.save(p);
            ctx.redirect("/project");
            // TODO IndexPage Refresh auslösen
        } else {
            put("title", "Edit project " + esc(p.getLastUrlPart().isEmpty() ? p.getUrl() : p.getLastUrlPart()));
            put("id", esc(p.getId()));
            put("url", esc(p.getUrl()));
            put("buildCommand", esc(p.getBuildCommand()));
            put("dir", esc(p.getDir()));
            put("login", esc(p.getUser().getLogin()));
            put("password", esc(p.getUser().getPassword()));
            put("branch", esc(p.getBranch()));
        }
    }
}
