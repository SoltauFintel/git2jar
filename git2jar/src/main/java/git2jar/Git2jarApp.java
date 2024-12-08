package git2jar;

import java.io.File;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import git2jar.base.Config;
import git2jar.base.FileService;
import git2jar.build.DeletePackageAction;
import git2jar.job.ClearDoneJobsAction;
import git2jar.job.CreateJobAction;
import git2jar.job.JobStatusPage;
import git2jar.project.AddProjectPage;
import git2jar.project.DeleteProjectAction;
import git2jar.project.EditProjectPage;
import git2jar.project.IndexPage;
import git2jar.project.ProjectsPage;
import git2jar.project.ReloadProjectsAction;
import git2jar.serve.FileRoute;
import github.soltaufintel.amalia.auth.simple.SimpleAuth;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.config.AppConfig;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import spark.Route;
import spark.Spark;

/**
 * The git2jar project contains 2 applications: WEB and SERVE.
 * WEB contains project and build. Both apps share the repository folder.
 */
public class Git2jarApp {
    public static final String VERSION = "0.1.0";
    
    public static void main(String[] args) {
        String mode = System.getenv("MODE");
        if ("SERVE".equalsIgnoreCase(mode)) {
            serve();
        } else if ("WEB".equalsIgnoreCase(mode)) {
            runWeb();
        } else {
            System.err.println("Please set env var MODE to 'SERVE' or 'WEB'. Unsupported mode: " + mode);
            System.exit(500);
        }
    }

	private static WebAppBuilder getWebAppBuilder() {
		return new WebAppBuilder(VERSION)
            .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
            .withInitializer(c -> Config.config = new Config(c));
	}

    private static void runWeb() {
        getWebAppBuilder()
            .withAuth(new SimpleAuth(new AppConfig())) // TODO Amalia withAuth: Ich brauch hier die config.
            .withTemplatesFolders(Git2jarApp.class, "/templates")
            .withRoutes(new WebRoutes())
            .build()
            .boot();
        System.out.println("==== web mode ====");
        FileService.deleteDir(Config.config.getJobsWorkDir());
    }
    
    private static class WebRoutes extends RouteDefinitions {

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
    
    private static void serve() {
        // not password protected
        getWebAppBuilder()
            .withRoutes(new ServeRoutes())
            .build()
            .boot();
        System.out.println("==== serve mode ====");
        File dir = Config.config.getRepositoryDir();
        if (!dir.isDirectory()) {
            Logger.error("Repository folder does not exist: " + dir.getAbsolutePath());
        }
    }
    
    private static class ServeRoutes extends RouteDefinitions {
        
        @Override
        public void routes() {
            Spark.head("/*", new FileRoute());
            Spark.get("/*", new FileRoute());

    		Route route404 = (req, res) -> {
    			Logger.warn(req.requestMethod() + " " + req.pathInfo());
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
}
