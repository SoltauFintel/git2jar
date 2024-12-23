package git2jar.project;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

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
            DataList list2 = map.list("tags");
            List<Tag> tags;
			try {
				tags = p.getTags(sv, full ? 0 : MAX_TAGS);
			} catch (Exception e) {
				Logger.error(e);
				tags = List.of();
			}
			for (Tag tag : tags) {
				DataMap map2 = list2.add();
				map2.put("tag", esc(tag.getTag()));
				map2.put("built", tag.isBuilt());
				List<File> folders = sv.getFolders(p);
				String implementation = "";
				String a = "implementation '" + p.getGroup() + ":", b = ":" + tag.getTag() + "'";
				if (folders != null && !folders.isEmpty()) {
					implementation = folders.stream().map(file -> a + file.getName() + b)
							.collect(Collectors.joining("\n"));
				} else {
					implementation = a + p.getLastUrlPart() + b;
				}
				map2.put("implementation", esc(implementation));
			}
            map.put("empty", list2.isEmpty());
        }
    }
}
