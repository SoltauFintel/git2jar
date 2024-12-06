package git2jar.project;

import java.util.List;

import com.github.template72.data.DataList;

import github.soltaufintel.amalia.web.action.Page;

public class ProjectsPage extends Page {

    @Override
    protected void execute() {
        List<Project> projects = new ProjectService().list().getProjects();

        put("title", "Projects");
        DataList list = list("projects");
        for (Project p : projects) {
            list.add().put("id", esc(p.getId())).put("url", esc(p.getUrl()));
        }
    }
}
