package git2jar.web;

import git2jar.project.AddProjectPage;
import git2jar.project.DeleteProjectAction;
import git2jar.project.EditProjectPage;
import git2jar.project.IndexPage;
import git2jar.project.ProjectsPage;
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
        form("/project/:id", EditProjectPage.class);
        get("/project", ProjectsPage.class);
        
        // TODO Project auswählen -> tags anbieten -> Build
        //      Vielleicht kann ich ja sehr schnell erkennen, welche Projekte neue tags haben?
        
        // TODO tags zu einem Repo besorgen, ohne dass das Repo (vollständig) geladen werden muss (bare?)
    }
}
