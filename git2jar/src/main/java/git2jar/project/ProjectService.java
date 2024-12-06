package git2jar.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import git2jar.base.FileService;
import git2jar.config.Config;
import github.soltaufintel.amalia.base.IdGenerator;

public class ProjectService {
    private static final String HANDLE = "HANDLE_P";
    
    public Projects list() {
        synchronized (HANDLE) {
            Projects projects = FileService.loadJsonFile(file(), Projects.class);
            if (projects == null  ) {
                return new Projects();
            }
            projects.getProjects().sort((a, b) -> a.getUrl().compareTo(b.getUrl()));
            return projects;
        }
    }
    
    public void save(Projects projects) {
        synchronized (HANDLE) {
            projects.getProjects().sort((a, b) -> a.getUrl().compareTo(b.getUrl()));
            FileService.saveJsonFile(file(), projects);
        }
    }

    public void create(String url) {
        Project p = new Project();
        p.setId(IdGenerator.createId6());
        p.setUrl(url);
        p.setBuildCommand("sh ./gradlew -Pgroup={group} -Pversion={tag} -xtest assemble publishToMavenLocal");

        synchronized (HANDLE) {
            Projects projects = list();
            projects.getProjects().add(p);
            save(projects);
        }
    }
    
    public Project get(String id) {
        synchronized (HANDLE) {
            return list().getProjects().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
        }
    }
    
    public void save(Project p) {
        synchronized (HANDLE) {
            Projects projects = list();
            projects.getProjects().removeIf(i -> i.getId().equals(p.getId()));
            projects.getProjects().add(p);
            save(projects);
        }
    }

    public void delete(String id) {
        synchronized (HANDLE) {
            Projects projects = list();
            if (projects.getProjects().removeIf(i -> i.getId().equals(id))) {
                save(projects);
            }
        }
    }
    
    private File file() {
        return new File(Config.config.getDataDir(), "projects.json");
    }
    
    public static class Projects {
        private final List<Project> projects = new ArrayList<>();

        public List<Project> getProjects() {
            return projects;
        }
    }
    
    public TreeSet<Tag> getTags(Project p) {
        TreeSet<Tag> tags = new TreeSet<>();
        
        // TODO Das ist Fake data.
        tags.add(new Tag("0.12.0", true));
        tags.add(new Tag("0.13.0", true));
        tags.add(new Tag("0.13.1", false));
        
        return tags;
    }
}
