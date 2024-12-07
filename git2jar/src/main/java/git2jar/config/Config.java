package git2jar.config;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;

public class Config {
    public static Config config;

    /** Das basedir enthält die weiteren Ordner. */
    private final String basedir;
    /** Im filesdir sind die Artefakte. */
    private final String filesdir;
    /** Im workdir sind die Workspaces (Git, Build). */
    private final String workdir;
    /** Im datadir sind die Projekte. */
    private final String datadir;
    
    public Config(AppConfig c) {
        basedir = c.get("basedir");
        if (basedir == null || basedir.isEmpty()) {
            throw new RuntimeException("Missing config property 'basedir'!");
        }
        filesdir = c.get("files", "files");
        workdir = c.get("workdir", "workdir");
        datadir = c.get("datadir", "data");
        Logger.debug("Config basedir: " + basedir);
        getWorkdir().mkdirs();
        getFilesDir().mkdirs();
        getDataDir().mkdirs();
    }
    
    private File getWorkdir() {
        return new File(basedir, workdir);
    }

    public File getTagsWorkDir() {
    	return new File(getWorkdir(), "tags");
    }

    public File getJobsWorkDir() {
    	return new File(getWorkdir(), "jobs");
    }

    public File getFilesDir() {
        return new File(basedir, filesdir);
    }
    
    public File getDataDir() {
        return new File(basedir, datadir);
    }
}
