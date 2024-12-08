package git2jar;

import org.pmw.tinylog.Level;

import git2jar.base.Config;
import git2jar.project.Web;
import git2jar.serve.Serve;
import github.soltaufintel.amalia.auth.simple.SimpleAuth;
import github.soltaufintel.amalia.web.builder.LoggingInitializer;
import github.soltaufintel.amalia.web.builder.WebAppBuilder;
import github.soltaufintel.amalia.web.config.AppConfig;

/**
 * Es gibt 2 Programme: WEB und SERVE.
 * WEB enthält Project und Build.
 * Beide Programme teilen sich einen Artefakte Ordner.
 */
public class Git2jarApp {
    public static final String VERSION = "0.1.0";
    
    public static void main(String[] args) {
        String mode = System.getenv("MODE");
        // Here are two webapps in one. Start in SERVE mode to serve files, start in WEB mode to show the password
        // protected webapp for building.
        if ("SERVE".equalsIgnoreCase(mode)) {
            serve();
        } else if ("WEB".equalsIgnoreCase(mode)) {
            runWeb();
        } else {
            System.err.println("Please set env var MODE to 'SERVE' or 'WEB'. Unsupported mode: " + mode);
        }
    }

    private static void runWeb() {
        new WebAppBuilder(VERSION)
            .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
            .withInitializer(c -> Config.config = new Config(c))
            .withAuth(new SimpleAuth(new AppConfig())) // TODO Amalia withAuth: Ich brauch hier die config.
            .withTemplatesFolders(Web.class, "/templates")
            .withRoutes(new Web())
            .build()
            .boot();
        System.out.println("==== web mode ====");
    }
    
    private static void serve() {
        // not password protected
        new WebAppBuilder(VERSION)
            .withLogging(new LoggingInitializer(Level.INFO, "{date} {level}  {message}"))
            .withInitializer(c -> Config.config = new Config(c))
            .withRoutes(new Serve())
            .build()
            .boot();
        System.out.println("==== serve mode ====");
    }
}
