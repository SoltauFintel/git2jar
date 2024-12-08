package git2jar.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

import git2jar.base.Config;
import git2jar.base.FileService;
import git2jar.build.GitService;
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
            if (_projects.getProjects().removeIf(i -> i.getId().equals(id))) {
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
		return stream.map(tag -> new Tag(tag.getTag(), new File(dir, tag.getTag()).exists())).toList();
    }
    
    public File getProjectFilesDir(Project p) {
		return new File(Config.config.getRepositoryDir().getAbsolutePath(), p.getGroupDir());
    }

	public void deletePackage(String id, String tag) {
		Project p = get(id);
		File dir = new File(getProjectFilesDir(p), tag);
		if (dir.isDirectory()) {
			FileService.deleteDir(dir);
			Logger.debug("deleted dir: " + dir.getAbsolutePath());
			
			if (FileService.isDirEmpty(dir.getParentFile())) {
				FileService.deleteDir(dir.getParentFile());
				Logger.debug("deleted also now empty parent dir: " + dir.getParentFile().getAbsolutePath());
			}
		}
		clearCache();
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
