package git2jar.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pmw.tinylog.Logger;

import git2jar.base.ShellScriptExecutor;
import git2jar.build.Job.JobStatus;
import git2jar.config.Config;
import git2jar.project.Project;
import git2jar.project.ProjectService;

public class BuildService {
	public static final List<Job> jobs = new ArrayList<>();
    // TODO Build soll in einem extra Container laufen, der nur während des Builds existiert.
    // TODO Ergebnisdateien dem serve-Programm zur Verfügung stellen

	/**
	 * Get job or creates job if it does not exist
	 * @param id project ID
	 * @param tag version
	 * @return Job
	 */
	public Job getStatus(String id, String tag) {
		return jobs.stream().filter(job -> job.getProject().getId().equals(id) && job.getTag().equals(tag)).findFirst().orElse(createJob(id, tag));
	}
	
    private Job createJob(String id, String tag) {
    	Project p = new ProjectService().get(id);
    	Job job = new Job(p, tag);
    	jobs.add(job);
    	startNextJob();
		return job;
	}

	public static void startNextJob() {
		Optional<Job> next = BuildService.jobs.stream().filter(j -> j.getStatus() == JobStatus.WAITING).findFirst();
		if (next.isPresent()) {
			// start next job
			next.get().start();
		}
	}

	public BuildResult build(Project project, String tag) {
        BuildResult ret = new BuildResult();
        String name = project.getUrl();
        name = name.substring(name.lastIndexOf("/") + 1); // TO-DO Name später aus gesamter URL ermitteln

        // Step 1: clone/pull
        File workspace = clone(project, tag, name);
        
        // Step 2: build
        String log = build(project, workspace);
                
        ret.setSuccess(log != null && log.contains("BUILD SUCCESSFUL"));
        ret.setLog(log);
        return ret;
    }

    private File clone(Project project, String tag, String name) {
        File workspace = new File(Config.config.getWorkdir(), name);
        GitService git = new GitService(workspace);
        if (workspace.isDirectory()) {
            Logger.info("pull: " + project.getUrl() + " | workspace: " + workspace.getAbsolutePath());
            git.pull(project.getUser());
        } else {
            Logger.info("clone: " + project.getUrl() + " | workspace: " + workspace.getAbsolutePath());
            git.clone(project.getUrl(), project.getUser(), tag, false);
        }
        Logger.info("switching to tag " + tag);
        git.selectCommit(tag);
        return workspace;
    }

    private String build(Project project, File workspace) {
        File dir = new File(workspace, project.getDir());
        String cmd = "cd " + dir.getAbsolutePath() + " && " + project.getBuildCommand();
        Logger.info("build command: " + cmd);
        return new ShellScriptExecutor().executeAndGetLog(cmd, dir);
    }
}
