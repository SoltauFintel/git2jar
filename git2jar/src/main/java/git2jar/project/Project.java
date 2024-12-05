package git2jar.project;

import git2jar.build.User;

public class Project {
    /** Git project URL */
    private String url; // TODO Funktioniert das auch wenn ".git" am Ende ist?
    private User user = new User();
    private String dir;
    private String buildCommand;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        if (url != null && url.contains("/")) {
            dir = url.substring(url.lastIndexOf("/") + 1);
        }
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
