package git2jar;

import java.io.File;

import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;

import git2jar.base.Config;
import git2jar.base.FileService;
import git2jar.base.Git2jarAuth;
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
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.route.RouteDefinitions;
import spark.Route;
import spark.Spark;

public final class Git2jarApp extends RouteDefinitions {
    public static final String VERSION = "0.2.1";
    
    // TODO auth/logout wird nicht gefunden
    // TODO wenn der eine Anfrage bekommt, aber die Lib nicht da ist, das Project bekannt ist, dann muss er das onthefly bauen

    @Override
    public void routes() {
        Spark.get("/", (req, res) -> { res.redirect("/project/home"); return ""; });
        get("/project/home", IndexPage.class);
        form("/project/add", AddProjectPage.class);
        get("/project/delete", DeleteProjectAction.class);
        get("/project/reload", ReloadProjectsAction.class);
        get("/project/clear-done-jobs", ClearDoneJobsAction.class);
        form("/project/:id", EditProjectPage.class);
        get("/project", ProjectsPage.class);
        
        get("/project/:id/:tag/build", CreateJobAction.class);
        get("/job/:jobId", JobStatusPage.class);
        get("/project/:id/:tag/delete", DeletePackageAction.class);

        Route info = (req, res) -> "git2jar " + VERSION;
        Spark.get("/rest/info", info);
        Spark.get("/rest/_info", info);

        Spark.head("/*", new FileRoute());
        Spark.get("/*", new FileRoute());
    }
    
    public static void main(String[] args) {
        new WebAppBuilder(VERSION)
            .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
            .withAuth(config -> new Git2jarAuth(config))
            .withTemplatesFolders(Git2jarApp.class, "/templates")
            .withInitializer(c -> Config.config = new Config(c))
            .withRoutes(new Git2jarApp())
            .build()
            .boot();
        
        // web ----
        if (Config.config.getJobsWorkDir().isDirectory()) {
            FileService.deleteDir(Config.config.getJobsWorkDir());
        }
        
        // serve ----
        File dir = Config.config.getRepositoryDir();
        if (dir.isDirectory()) {
            Logger.info("serve dir: " + dir.getAbsolutePath());
        } else {
            Logger.error("Repository folder does not exist: " + dir.getAbsolutePath());
        }
    }
}
