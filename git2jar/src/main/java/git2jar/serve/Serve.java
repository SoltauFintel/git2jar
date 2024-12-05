package git2jar.serve;

import java.io.File;

import org.pmw.tinylog.Logger;

import git2jar.config.Config;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import spark.Spark;

public class Serve extends RouteDefinitions {
    
    public Serve() {
        File dir = Config.config.getFilesDir();
        if (!dir.isDirectory()) {
            Logger.error("files-dir does not exist: " + dir.getAbsolutePath());
        }
    }
    
    @Override
    public void routes() {
        Spark.head("/*", new FileRoute(true));
        Spark.get("/*", new FileRoute(false));
    }
}
