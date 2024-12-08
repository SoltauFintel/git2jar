package git2jar.project;

import git2jar.build.BuildPage;
import git2jar.build.ClearDoneJobsAction;
import git2jar.build.DeletePackageAction;
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
        get("/project/clear-done-jobs", ClearDoneJobsAction.class);
        form("/project/:id", EditProjectPage.class);
        get("/project", ProjectsPage.class);
        
        get("/project/:id/:tag/build", BuildPage.class);
        get("/project/:id/:tag/delete", DeletePackageAction.class);
    }
}
