package git2jar.project;

import java.util.List;
import java.util.TreeSet;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;

public class IndexPage extends Page {

    @Override
    protected void execute() {
        ProjectService sv = new ProjectService();
        List<Project> projects = sv.list().getProjects();

        put("title", "git2jar Homepage");
        DataList list = list("projects");
        for (Project p : projects) {
            DataMap map = list.add();
            map.put("id", esc(p.getId()));
            map.put("url", esc(p.getUrl()));
            map.put("ga", "bla.bla:" + p.getLastUrlPart()); // TODO !
            DataList list2 = map.list("tags");
            TreeSet<Tag> tags = sv.getTags(p);
            for (Tag tag : tags) { // TODO reverse order
                list2.add().put("tag", esc(tag.getTag())).put("built", tag.isBuilt());
            }
            map.put("empty", list2.isEmpty());
        }
    }
}
