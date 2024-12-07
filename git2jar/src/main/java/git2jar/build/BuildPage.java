package git2jar.build;

import github.soltaufintel.amalia.web.action.Page;

public class BuildPage extends Page {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String tag = ctx.pathParam("tag");

		Job job = new BuildService().getStatus(id, tag);
		
		put("title", "Building " + job.getProject().getLastUrlPart());
		put("url", esc(job.getProject().getUrl()));
		put("tag", esc(job.getTag()));
		put("status", esc(job.getStatus().name()));
	}
}
