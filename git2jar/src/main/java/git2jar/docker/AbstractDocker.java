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

public abstract class AbstractDocker {
    private final DockerClient docker;
    public String id;
    
    public AbstractDocker() {
        docker = createClient();
    }
    
    protected abstract DockerClient createClient();
    
    // TODO brauch ich das?
    void pull(String image) {
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

    // TODO etwaige git2jar Logik hier raus nehmen!
    public String run(String image, File dir) {
		List<Bind> binds = new ArrayList<>();
		binds.add(new Bind(dir.getAbsolutePath(), new Volume("/work"), AccessMode.rw));
    	
        id = docker.createContainerCmd(image)
            .withHostConfig(new HostConfig().withBinds(binds))
            .exec().getId();
		Logger.info("created container with ID '" + id + "' from image '" + image + "'");
        
        docker.startContainerCmd(id).exec();
        Logger.info("Container started. Waiting for end...");
        
        // waiting for container end
        Info in = docker.infoCmd().exec();
        while (in.getContainersRunning() > 0) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
        	in = docker.infoCmd().exec();
        }
        Logger.info("Container ended.");
        
        // retrieve logs
        String logs = "";
        for (int i = 1; i <= 6; i++) {
            try {
                Thread.sleep(2 * 1000);
            } catch (Exception e) {
            }
            logs = logs(id);
            if (logs != null && !logs.isEmpty()) {
                break;
            }
        }
        return logs;
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
}
