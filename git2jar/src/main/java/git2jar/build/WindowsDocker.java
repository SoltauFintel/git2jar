package git2jar.build;

import org.pmw.tinylog.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class WindowsDocker extends AbstractDocker {

	public WindowsDocker() {
		super(createClient());
	}

	private static DockerClient createClient() {
        Logger.debug("WindowsDocker");
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://localhost:2375")
				.withDockerTlsVerify(false)
				.withApiVersion("1.41")
				.withRegistryUrl("https://index.docker.io/v1/")
				.build();
		return DockerClientBuilder.getInstance(config).build();
	}
}
