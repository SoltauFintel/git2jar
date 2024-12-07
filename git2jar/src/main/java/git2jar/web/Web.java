package git2jar.web;

import git2jar.build.BuildPage;
import git2jar.build.DeletePackageAction;
import git2jar.project.AddProjectPage;
import git2jar.project.DeleteProjectAction;
import git2jar.project.EditProjectPage;
import git2jar.project.IndexPage;
import git2jar.project.ProjectsPage;
import git2jar.project.ReloadProjectsAction;
import github.soltaufintel.amalia.web.route.RouteDefinitions;

/**
 * Git2jar Web frontend
 * 
 * password protected
 */
public class Web extends RouteDefinitions {

    @Override
    public void routes() {
        get("/", IndexPage.class);
        form("/project/add", AddProjectPage.class);
        get("/project/delete", DeleteProjectAction.class);
        get("/project/reload", ReloadProjectsAction.class);
        form("/project/:id", EditProjectPage.class);
        get("/project", ProjectsPage.class);
        
        get("/project/:id/:tag/build", BuildPage.class);
        get("/project/:id/:tag/delete", DeletePackageAction.class);
    }
}
