package git2jar.project;

import java.io.File;

import org.pmw.tinylog.Logger;

import git2jar.build.BuildResult;
import git2jar.build.BuildService;
import github.soltaufintel.amalia.web.config.AppConfig;

public class Testmode {

    public void run() {
        AppConfig config = new AppConfig();
        
        String tag = config.get("tag");

        Project project = new Project();
        project.setUrl(config.get("url"));
        project.setBuildCommand(config.get("buildcmd").replace("{tag}", tag));

        BuildResult r = new BuildService().build(project, tag);
        
        Logger.info("---------------------------------------------------------");
        Logger.info(r.getLog());
        Logger.info("---------------------------------------------------------");
        Logger.info("build success: " + (r.isSuccess() ? "yes" : "no"));
     
        Logger.info("searching...");
        File maven = new File(System.getProperty("user.home") + "/.m2/repository");
        if (maven.isDirectory()) {
            search(maven, tag, project.getDir());
        } else {
            Logger.error("Dir doesn't exist: " + maven.getAbsolutePath());
        }
    }

    private void search(File dir, String tag, String x) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String n = file.getName();
                if (file.isFile() && n.contains(tag) && (n.contains(x) || n.endsWith(".pom") || n.endsWith(".jar"))) {
                    Logger.info("** " + file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    search(file, tag, x); // recursive
                }
            }
        }
    }
}
