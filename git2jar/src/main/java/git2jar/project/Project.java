package git2jar.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private String branch = "master";
	private transient Map<String, List<Tag>> tags = new HashMap<>();

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
		String ret = url != null && url.contains("/") ? url.substring(url.lastIndexOf("/") + 1) : "";
		if (ret.endsWith(".git")) {
			ret = ret.substring(0, ret.length() - ".git".length());
		}
		return ret;
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

	public String getGroup() {
		String ret = getRawGroup();
		if (!ret.isEmpty() && dir.isEmpty()) {
			ret += "." + getLastUrlPart();
		}
		return ret.toLowerCase();
	}

	public String getRawGroup() {
		if (url == null) {
			return "";
		}
		String ret = "";
		int o = url.indexOf("//") + "//".length();
		if (o < 0) {
			return "";
		}
		int oo = url.indexOf("/", o);
		if (oo < 0) {
			return "";
		}
		int ooo = url.indexOf("/", oo + 1);
		if (ooo < 0) {
			return "";
		}
		for (String w : url.substring(o, oo).split("\\.")) {
			if (!ret.isEmpty()) {
				ret = "." + ret;
			}
			ret = w + ret;
		}
		ret += "." + url.substring(oo + 1, ooo);
		return ret.toLowerCase();
	}

	public String getGroupDir() {
		return getRawGroup().replace(".", "/") + "/" + getLastUrlPart();
	}

	public List<Tag> getTags(ProjectService sv, int limit) {
		List<Tag> ret = tags.get("" + limit);
		if (ret == null) {
			ret = sv.getTags(this, limit);
			tags.put("" + limit, ret);
		}
		return ret;
	}
	
	public void clearCache() {
		tags.clear();
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}
}
