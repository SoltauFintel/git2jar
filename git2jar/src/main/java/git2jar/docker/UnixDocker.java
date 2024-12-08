package git2jar.docker;

import org.pmw.tinylog.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import github.soltaufintel.amalia.web.config.AppConfig;

public class UnixDocker extends AbstractDocker {
	
	public UnixDocker() {
		super(createClient());
	}

	private static DockerClient createClient() {
		Logger.debug("UnixDocker");
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock")
				.withApiVersion(getApiVersion())
				.withRegistryUrl("https://index.docker.io/v1/")
				.build();
		return DockerClientBuilder.getInstance(config).build();
	}
	
	public static String getApiVersion() {
		AppConfig cfg = new AppConfig();
		return cfg.get("docker.version", "1.26");
	}
}
