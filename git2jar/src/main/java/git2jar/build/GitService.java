package git2jar.build;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.pmw.tinylog.Logger;

/**
 * Control Git repository with quite low level functions: clone, fetch, pull, tag, branch, commit, select branch/commit.
 */
public class GitService {
    private final File workspace;
    
    public GitService(File workspace) {
        this.workspace = workspace;
    }
    
    /**
     * Git clone action
     * <p>This call may take several minutes.</p>
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
    
    /**
     * Git fetch action
     * @param user user to log into remote Git repository
     */
    public void fetch(User user) {
        try (Git git = Git.open(workspace)) {
            git.fetch()
                .setCredentialsProvider(getUsernamePasswordCredentialsProvider(user))
                .call();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Git repository!", e);
        }
    }

    /**
     * Git pull action
     * <p>Please call clearTags() before if you want to ensure that deleted tags on remote do not exist in local repo.</p>
     * @param user user to log into remote Git repository
     */
    public void pull(User user) {
        try (Git git = Git.open(workspace)) {
            git.pull()
                .setCredentialsProvider(getUsernamePasswordCredentialsProvider(user))
                .call();
        } catch (Exception e) {
            throw new RuntimeException("Error pulling Git repository!", e);
        }
    }

    /**
     * This call may take 4 seconds.
     * @return false if there is a change in the work tree, e.g. an added or changed file
     * <br>true if there is nothing to be committed (but maybe something to push)
     * <br>Of course any changes in gitignore folders are not detected (e.g. build folder).
     */
    public boolean isWorkspaceClean() {
        try (Git git = Git.open(workspace)) {
            return git.status().call().isClean();
        } catch (Exception e) {
            throw new RuntimeException("Error while detecting if workspace is clean!", e);
        }
    }

    /**
     * checkout: Set HEAD to other commit
     * @param commit commit hash, can be short form
     * A tag name should also work.
     */
    public void selectCommit(String commit) {
        try (Git git = Git.open(workspace)) {
            git.checkout()
                .setName(commit)
                .call();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error selecting commit! " + e.getMessage(), e);
        }
    }

    /**
     * checkout branch
     * @param branch e.g. "3.03.x"
     */
    public void switchToBranch(String branch) {
        try (Git git = Git.open(workspace)) {
            try {
                // try for locally existing branch
                git.checkout()
                    .setName(branch)
                    .call();
            } catch (RefNotFoundException e) {
                // create local branch from remote tracking branch
                git.checkout()
                    .setName(branch)
                    .setCreateBranch(true)
                    .call();
            }            
        } catch (Exception e) {
            throw new RuntimeException("Error while switching branch to " + branch, e);
        }
    }

    /**
     * @return hash of current commit (HEAD), e.g. "f65bb8c600a3ea1eabdbdcad1f6bd381f00636b6"
     */
    public String getCurrentCommitHash() {
        try (Git git = Git.open(workspace)) {
            Iterator<RevCommit> iter = git.log().setMaxCount(1).call().iterator();
            return iter.hasNext() ? iter.next().getName() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error getting current commit hash!", e);
        }
    }
    
    /**
     * @return e.g. "master"
     */
    public String getCurrentBranch() {
        try (Git git = Git.open(workspace)) {
            return git.getRepository().getBranch();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean areThereRemoteUpdates(String targetBranch, User user) {
        try (Git git = Git.open(workspace)) {
            FetchResult f = git.fetch()
                    .setCredentialsProvider(getUsernamePasswordCredentialsProvider(user))
                    .call();
            long n = f.getTrackingRefUpdates().stream().filter(i -> i.getRemoteName().endsWith("/" + targetBranch)).count();
            return n > 0;
        } catch (Exception e) {
            Logger.error(e);
            return true;
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
