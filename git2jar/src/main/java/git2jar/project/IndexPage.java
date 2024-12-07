package git2jar.project;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;

public class IndexPage extends Page {
	private static final int MAX_TAGS = 3;
	
    @Override
    protected void execute() {
    	boolean full = "full".equals(ctx.queryParam("m"));
    	
        ProjectService sv = new ProjectService();
		List<Project> projects = sv.list().getProjects();

        put("title", "git2jar Homepage");
        put("deleteAllowed", true); // only developer can delete for testing
        put("full", full);
        DataList list = list("projects");
        for (Project p : projects) {
            DataMap map = list.add();
            map.put("id", esc(p.getId()));
            map.put("url", esc(p.getUrl()));
            map.put("ga", p.getGroup() + ":" + p.getLastUrlPart());
            DataList list2 = map.list("tags");
            for (Tag tag : p.getTags(sv, full ? 0 : MAX_TAGS)) {
				DataMap map2 = list2.add();
				map2.put("tag", esc(tag.getTag()));
				map2.put("built", tag.isBuilt());
            }
            map.put("empty", list2.isEmpty());
        }
    }
}
