package git2jar.build;

import github.soltaufintel.amalia.web.action.Page;

public class BuildPage extends Page {

	@Override
	protected void execute() {
		// TODO Is build in job queue?
		//      yes: show job status
		//      no: create job and put it into the queue
		// Queue can only work at one job at a time.
		
		put("title", "Build");
	}
}
