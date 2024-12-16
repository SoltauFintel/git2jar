package git2jar.base;

import java.io.File;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.config.AppConfig;

public final class Config {
    public static Config config;

    /** persistent folder containing projects data */
    private final File projects;
    /** persistent folder containing artifacts, used by WEB and SERVE */
    private final File repository;
    /** Work directory for getting tags from Git repositories. It's temporary but kept for improving performance. */
    private final File tags;
    /** Work directory for executing jobs. It's temporary and will be deleted after job execution or at app startup. */
    private final File jobs;
    /** name of Docker image */
    private final String image;
    /** name of Docker base image */
    private final String baseImage;
    
    public Config(AppConfig c) {
        String basedir = c.get("basedir"); // base folder containing the other folders
        if (basedir == null || basedir.isEmpty()) {
            throw new RuntimeException("Config option 'basedir' is not set!");
        }
        Logger.debug("Config basedir: " + basedir);
        projects = new File(basedir, c.get("projects", "projects"));
        repository = new File(basedir, c.get("repository", "repository"));
        tags = new File(basedir, c.get("tags", "tags"));
        jobs = new File(basedir, c.get("jobs", "jobs"));
        image = c.get("image", "git2jar-jdk17");
		if (image == null || image.isBlank()) {
			throw new RuntimeException("Config option 'image' is not set!");
		}
        baseImage = c.get("base-image", "eclipse-temurin:17.0.7_7-jdk-alpine");
    }
    
    public File getProjectsDir() {
        return projects;
    }
    
    public File getRepositoryDir() {
        return repository;
    }

    public File getTagsWorkDir() {
		return tags;
    }

    public File getJobsWorkDir() {
    	return jobs;
    }

    public String getImage() {
    	return image;
    }

	public String getBaseImage() {
		return baseImage;
	}
}
