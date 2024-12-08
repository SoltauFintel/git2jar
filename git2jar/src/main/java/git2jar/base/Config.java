package git2jar.base;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;

public class Config {
    public static Config config;

    /** base folder containing the other folders */
    private final String basedir;
    /** folder containing artifacts */
    private final File repository;
    /** work directory */
    private final File work;
    /** folder containing projects data */
    private final File projects;
    /** name of Docker image */
    private final String image;
    
    public Config(AppConfig c) {
        basedir = c.get("basedir");
        if (basedir == null || basedir.isEmpty()) {
            throw new RuntimeException("Config option 'basedir' is not set!");
        }
        Logger.debug("Config basedir: " + basedir);
        repository = new File(basedir, c.get("repository", "repository"));
        work = new File(basedir, c.get("work", "work"));
        projects = new File(basedir, c.get("projects", "projects"));
        image = c.get("image", "git2jar-jdk17");
		if (image == null || image.isBlank()) {
			throw new RuntimeException("Config option 'image' is not set!");
		}
    }
    
    public File getTagsWorkDir() {
		return new File(work, "tags");
    }

    public File getJobsWorkDir() {
    	return new File(work, "jobs");
    }

    public File getRepositoryDir() {
        return repository;
    }
    
    public File getProjectsDir() {
        return projects;
    }
    
    public String getImage() {
    	return image;
    }
}
