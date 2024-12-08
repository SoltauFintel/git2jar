package git2jar.serve;

import java.io.File;

import org.pmw.tinylog.Logger;

import git2jar.base.Config;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import spark.Route;
import spark.Spark;

public class Serve extends RouteDefinitions {
    
    private void init2() {
        File dir = Config.config.getFilesDir();
        if (!dir.isDirectory()) {
            Logger.error("files-dir does not exist: " + dir.getAbsolutePath());
        }
    }
    
    @Override
    public void routes() {
    	init2();
        Spark.head("/*", new FileRoute());
        Spark.get("/*", new FileRoute());

		Route route404 = (req, res) -> {
			Logger.info(req.requestMethod() + " " + req.pathInfo());
			res.status(404);
			return "Unsupported endpoint";
		};
		Spark.post("/*", route404);
		Spark.put("/*", route404);
		Spark.delete("/*", route404);
		Spark.options("/*", route404);
		Spark.connect("/*", route404);
		Spark.trace("/*", route404);
    }
}
