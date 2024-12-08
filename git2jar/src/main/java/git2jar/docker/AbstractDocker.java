package git2jar.docker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Volume;

import git2jar.base.ShellScriptExecutor;
import git2jar.build.BuildResult;

public abstract class AbstractDocker {
    private final DockerClient docker;
    private long timeout = 20;
    
    public AbstractDocker(DockerClient docker) {
        this.docker = docker;
    }
    
    public void pull(String image) {
        try {
        	if (!image.contains(":")) {
        		image += ":latest";
        	}
        	Logger.debug("pulling image: " + image);
            docker.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion();
        	Logger.debug("pulling image: " + image + " => ok");
        } catch (Exception e) {
        	Logger.debug("Error pulling image: " + image);
            Logger.error(e);
        }
    }

    public BuildResult run(String image, File hostDir, String containerDir) {
		List<Bind> binds = new ArrayList<>();
		binds.add(new Bind(hostDir.getAbsolutePath(), new Volume(containerDir), AccessMode.rw));
    	
        String id = docker.createContainerCmd(image)
            .withHostConfig(new HostConfig().withBinds(binds))
            .exec().getId();
        BuildResult ret = new BuildResult();
        ret.setId(id);
		Logger.info("created container with ID '" + id + "' from image '" + image + "'");
        
        docker.startContainerCmd(id).exec();
        Logger.info("Container started. Waiting for end...");
        
        // waiting for container end
        long start = System.currentTimeMillis();
        Info in = docker.infoCmd().exec();
        while (in.getContainersRunning() > 0) {
        	try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
        	long duration = (System.currentTimeMillis() - start) / 1000;
			if (duration > timeout * 60) {
				String log = "";
				try {
					log = logs(id); // retrieve logs
				} catch (Exception ignore) {
				}
				ret.setLog("Timeout after " + duration + "s\n" + log);
        		return ret;
        	}
        	
        	in = docker.infoCmd().exec();
        }
        Logger.info("Container ended.");
        ret.setLog(logs(id)); // retrieve logs
        return ret;
    }
    
    public String logs(String container) {
        StringBuffer sb = new StringBuffer();
        try {
            LogContainerCmd logContainerCmd = docker.logContainerCmd(container);
            logContainerCmd.withStdOut(true).withStdErr(true);
            logContainerCmd.exec(new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame item) {
                    sb.append(item.toString());
                    sb.append("\n");
                }
            }).awaitCompletion(10, TimeUnit.SECONDS);
            return sb.toString();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void rmf(String container) {
        try {
            docker.removeContainerCmd(container).withForce(Boolean.TRUE).exec();
        } catch (Exception e) {
            Logger.error(e, "Error deleting container: " + container);
        }
    }

	public long getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout minutes, must be greater than 0
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public static AbstractDocker get() {
		AbstractDocker docker;
		if (ShellScriptExecutor.isWindows()) {
			docker = new WindowsDocker();
		} else {
			docker = new UnixDocker();
		}
		return docker;
	}
}
