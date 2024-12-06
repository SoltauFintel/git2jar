package git2jar.project;

import git2jar.build.User;

public class Project {
    private String id;
    // TODO UserId (Owner)
    /** Git project URL */
    private String url; // TODO Funktioniert das auch wenn ".git" am Ende ist?
    /** Git user/credentials */
    private User user = new User();
    /** folder in in Git repository */
    private String dir;
    /** Gradle build command */
    private String buildCommand;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }
    
    public String getLastUrlPart() {
        return url != null && url.contains("/") ? url.substring(url.lastIndexOf("/") + 1) : "";
    }

    public void setUrl(String url) {
        this.url = url;
        dir = getLastUrlPart();
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getBuildCommand() {
        return buildCommand;
    }

    public void setBuildCommand(String buildCommand) {
        this.buildCommand = buildCommand;
    }
}
