package git2jar.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

import git2jar.base.Config;
import git2jar.base.FileService;
import github.soltaufintel.amalia.base.IdGenerator;

public class ProjectService {
    private static final String HANDLE = "HANDLE_P";
    public static Projects _projects;

    public void clearCache() {
    	_projects = null;
    	list();
    }
    
    public Projects list() {
        synchronized (HANDLE) {
        	if (_projects == null) {
	            Projects projects = FileService.loadJsonFile(file(), Projects.class);
				if (projects == null) {
					_projects = new Projects();
				} else {
		            projects.getProjects().sort((a, b) -> a.getUrl().compareTo(b.getUrl()));
		            _projects = projects;
				}
        	}
        	return _projects;
        }
    }
    
    public void save() {
        synchronized (HANDLE) {
            _projects.getProjects().sort((a, b) -> a.getUrl().compareTo(b.getUrl()));
            FileService.saveJsonFile(file(), _projects);
        }
    }

    public void create(String url) {
        Project p = new Project();
        p.setId(IdGenerator.createId6());
        p.setUrl(url);
        p.setBuildCommand("sh ./gradlew -Pgroup={group} -Pversion={tag} -xtest assemble publishToMavenLocal");

        synchronized (HANDLE) {
            list();
            _projects.getProjects().add(p);
            save();
        }
    }
    
    public Project get(String id) {
        synchronized (HANDLE) {
            return list().getProjects().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        }
    }
    
    public void save(Project p) {
        synchronized (HANDLE) {
            list();
            _projects.getProjects().removeIf(i -> i.getId().equals(p.getId()));
            _projects.getProjects().add(p);
            save();
        }
    }

    public void delete(String id) {
        synchronized (HANDLE) {
            list();
			if (_projects.getProjects().removeIf(project -> {
				boolean delete = project.getId().equals(id);
				if (delete) {
					File dir = new File(Config.config.getTagsWorkDir(), project.getLastUrlPart());
					if (dir.isDirectory()) {
						FileService.deleteDir(dir);
					}
				}
				return delete;
			})) {
				save();
			}
        }
    }
    
    public List<Tag> getTags(Project p, int limit) {
    	File workspace = new File(Config.config.getTagsWorkDir(), p.getLastUrlPart());
    	GitService git = new GitService(workspace);
    	if (workspace.isDirectory()) {
    		git.fetch(p.getUser());
    	} else {
    		git.clone(p.getUrl(), p.getUser(), p.getBranch(), true);
    	}
		Stream<Tag> stream = git.getTags().stream().map(tag -> new Tag(tag, false))
				.sorted((a, b) -> b.sort().compareTo(a.sort()));
		if (limit > 0) {
			stream = stream.limit(limit);
		}
		File dir = getProjectFilesDir(p);
		return stream.map(tag -> new Tag(tag.getTag(), hasFile(p, dir, tag.getTag()))).toList();
    }
    
    private boolean hasFile(Project p, File dir, String tag) {
		if (p.getDir().isEmpty()) {
			File[] files = dir.listFiles();
			if (files == null) {
				return false;
			}
			int countValidFolders = 0, countVersionFolders = 0;
			for (File file : files) {
				if (!file.getName().startsWith(".") && file.isDirectory()) {
					countValidFolders++;
					if (new File(file, tag).isDirectory()) {
						countVersionFolders++;
					}
				}
			}
			return countValidFolders > 0 && countValidFolders == countVersionFolders;
		} else {
			return new File(dir, tag).exists();
		}
	}
    
	public List<File> getFolders(Project p) {
		if (p.getDir().isEmpty()) {
			File dir = getProjectFilesDir(p);
			File[] files = dir.listFiles();
			if (files != null) {
				List<File> ret = new ArrayList<>();
				for (File file : files) {
					if (!file.getName().startsWith(".") && file.isDirectory()) {
						ret.add(file);
					}
				}
				return ret;
			}
		}
		return null;
	}
    
    public File getProjectFilesDir(Project p) {
		return new File(Config.config.getRepositoryDir().getAbsolutePath(), p.getGroupDir());
    }

	public boolean deletePackage(String id, String tag) {
		boolean ret = false;
		Project p = get(id);
		File dir = null;
		if (p.getDir().isEmpty() && getFolders(p) != null) {
			for (File file : getFolders(p)) {
				if (del(new File(file, tag))) {
					ret = true;
				}
				dir = file.getParentFile();
			}
		} else {
			dir = new File(getProjectFilesDir(p), tag);
			ret = del(dir);
		}
		if (dir != null && FileService.isDirEmpty(dir.getParentFile())) {
			FileService.deleteDir(dir.getParentFile());
			Logger.debug("deleted also now empty parent dir: " + dir.getParentFile().getAbsolutePath());
		}
		clearCache();
		return ret;
	}

	private boolean del(File dir) {
		if (dir.isDirectory()) {
			FileService.deleteDir(dir);
			Logger.debug("deleted dir: " + dir.getAbsolutePath());
			return true;
		}
		return false;
	}

    private File file() {
        return new File(Config.config.getProjectsDir(), "projects.json");
    }
    
    public static class Projects {
        private final List<Project> projects = new ArrayList<>();

        public List<Project> getProjects() {
            return projects;
        }
    }
}
