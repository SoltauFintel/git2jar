package git2jar.project;

import git2jar.build.DeletePackageAction;
import git2jar.job.ClearDoneJobsAction;
import git2jar.job.CreateJobAction;
import git2jar.job.JobStatusPage;
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
        
        get("/project/:id/:tag/build", CreateJobAction.class);
        get("/job/:jobId", JobStatusPage.class);
        get("/project/:id/:tag/delete", DeletePackageAction.class);
    }
}
