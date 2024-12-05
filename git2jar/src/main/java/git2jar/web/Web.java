package git2jar.web;

import git2jar.web.index.IndexPage;
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
    }
}
