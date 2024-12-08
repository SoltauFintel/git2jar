package git2jar.project;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.pmw.tinylog.Logger;

/**
 * Access Git repository
 */
public class GitService {
    private final File workspace;
    
    public GitService(File workspace) {
        this.workspace = workspace;
    }
    
    /**
     * @param url URL of remote Git repository
     * <br>Please be careful that there is no leading or trailing whitespace within the arguments!
     * This may end in an "Illegal character in path" URI exception.
     * @param user user to log into remote Git repository
     * @param branch which branch should be active after clone, e.g. "master"
     * @param bare false: with repository and with workspace, true: with repository and without workspace
     */
    public void clone(String url, User user, String branch, boolean bare) {
        try (Git result = Git.cloneRepository()
                .setURI(url)
                .setCredentialsProvider(getUsernamePasswordCredentialsProvider(user))
                .setBranch(branch)
                .setDirectory(workspace)
                .setBare(bare)
                .call()) {
        } catch (Exception e) {
            e.printStackTrace(); // Brauch ich für die Fehleranalyse. Ich habe den Verdacht, dass tinylog zu viel vom Stacktrace nicht ausgibt.
            Logger.error("Error cloning Git repository! URL: " + url + " | user: " + user.getLogin()
                + " | branch: " + branch);
            throw new RuntimeException("Error accessing Git repository! Please try logout and login.", e);
        }
    }
    
    public void fetch(User user) {
        try (Git git = Git.open(workspace)) {
            git.fetch()
                .setCredentialsProvider(getUsernamePasswordCredentialsProvider(user))
                .call();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Git repository!", e);
        }
    }

    private UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            return null;
        }
        return new UsernamePasswordCredentialsProvider(user.getLogin(), user.getPassword());
    }
    
    public List<String> getTags() {
    	final String prefix = "refs/tags/";
        try (Git git = Git.open(workspace)) {
			return git.tagList().call().stream().map(ref -> {
				String tag = ref.getName();
				if (tag.startsWith(prefix)) {
					tag = tag.substring(prefix.length());
				}
				return tag;
			})
			.toList();
        } catch (Exception e) {
        	throw new RuntimeException("Error loading tags", e);
        }
    }
}
