package git2jar.config;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;

public class Config {
    public static Config config;

    private final String basedir;
    private final String filesdir;
    private final String workdir;
    
    public Config(AppConfig c) {
        basedir = c.get("basedir");
        if (basedir == null || basedir.isEmpty()) {
            throw new RuntimeException("Missing config property 'basedir'!");
        }
        filesdir = c.get("files", "files");
        workdir = c.get("workdir", "workdir");
        Logger.debug("Config basedir: " + basedir);
        getWorkdir().mkdirs();
        getFilesDir().mkdirs();
    }
    
    public File getWorkdir() {
        return new File(basedir, workdir);
    }
    
    public File getFilesDir() {
        return new File(basedir, filesdir);
    }
}
