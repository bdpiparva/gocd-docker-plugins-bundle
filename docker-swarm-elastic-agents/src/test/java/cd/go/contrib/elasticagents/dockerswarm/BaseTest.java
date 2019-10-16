/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.elasticagents.dockerswarm;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.Container;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.SWARM_SERVICE_NAME;
import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.contrib.elasticagents.dockerswarm.utils.Util.dockerApiVersionAtLeast;
import static java.lang.System.getenv;
import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public abstract class BaseTest {
    private static DefaultDockerClient.Builder builder;
    static DefaultDockerClient docker;
    protected static HashSet<String> services;

    @BeforeAll
    public static void beforeClass() throws Exception {
        builder = DefaultDockerClient.fromEnv();
        docker = builder.build();
        services = new HashSet<>();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        for (String service : services) {
            try {
                docker.inspectService(service);
                docker.removeService(service);
            } catch (ServiceNotFoundException ignore) {

            }
        }
        removeSecrets();
        removeVolume();
    }

    private static void removeSecrets() throws Exception {
        if (dockerApiVersionAtLeast(docker, "1.26")) {
            docker.listSecrets().forEach(secret -> {
                if (secret.secretSpec().labels().containsKey("cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSwarmPlugin")) {
                    try {
                        docker.deleteSecret(secret.id());
                    } catch (DockerException | InterruptedException e) {
                    }
                }
            });
        } else {
            LOG.warn(format("Detected docker version and api version is {0} and {1} respectively. Docker with api version 1.26 or above is required to use volume mounts, secrets and host file entries. Please referhttps://docs.docker.com/engine/api/v1.32/#section/Versioning for more information about docker release.", docker.version().version(), docker.version().apiVersion()));
        }
    }

    private static void removeVolume() throws Exception {
        if (dockerApiVersionAtLeast(docker, "1.26")) {
            if (docker.listVolumes().volumes() != null) {
                docker.listVolumes().volumes().forEach(volume -> {
                    if (volume.labels() != null && volume.labels().containsKey("cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSwarmPlugin")) {
                        try {
                            docker.removeVolume(volume.name());
                        } catch (DockerException | InterruptedException e) {
                        }
                    }
                });
            }
        } else {
            LOG.warn(format("Detected docker version and api version is {0} and {1} respectively. Docker with api version 1.26 or above is required to use volume mounts, secrets and host file entries. Please refer https://docs.docker.com/engine/api/v1.32/#section/Versioning for more information about docker release.", docker.version().version(), docker.version().apiVersion()));
        }
    }

    protected SwarmClusterConfiguration createClusterProfiles() throws IOException {
        SwarmClusterConfiguration settings = new SwarmClusterConfiguration();
        settings.setMaxDockerContainers("1");
        settings.setDockerURI(builder.uri().toString());
        if (settings.getDockerURI().startsWith("https://")) {
            settings.setDockerCACert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CA_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            settings.setDockerClientCert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            settings.setDockerClientKey(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_KEY_NAME).toFile(), StandardCharsets.UTF_8));
        }

        return settings;
    }

    void assertServiceDoesNotExist(String id) throws DockerException, InterruptedException {
        try {
            docker.inspectService(id);
            fail("Expected ServiceNotFoundException");
        } catch (ServiceNotFoundException expected) {

        }
    }

    void assertServiceExist(String id) throws DockerException, InterruptedException {
        assertThat(docker.inspectService(id)).isNotNull();
    }

    static void requireDockerApiVersionAtLeast(final String required, final String functionality)
            throws Exception {
        final String msg = String.format("Docker API should be at least v%s to support %s but runtime version is %s", required, functionality, docker.version().apiVersion());
        if(!dockerApiVersionAtLeast(docker, required)) {
            throw new RuntimeException(msg);
        }
    }

    protected List<Container> waitForContainerToStart(DockerService service,
                                                      final int waitInSeconds) throws DockerException, InterruptedException {
        List<Container> containers = null;
        final AtomicInteger retry = new AtomicInteger();
        do {
            containers = docker.listContainers(DockerClient.ListContainersParam.withLabel(SWARM_SERVICE_NAME, service.name()), DockerClient.ListContainersParam.allContainers());
            Thread.sleep(1000);
        } while (containers.isEmpty() && retry.incrementAndGet() < waitInSeconds);

        if (containers.isEmpty()) {
            fail("Should start container.");
        }

        return containers;
    }

    protected SwarmClusterConfiguration createClusterProfileProperties() throws IOException {
        SwarmClusterConfiguration swarmClusterConfiguration = new SwarmClusterConfiguration();
        swarmClusterConfiguration.setMaxDockerContainers("1");
        swarmClusterConfiguration.setDockerURI(builder.uri().toString());
        if (swarmClusterConfiguration.getDockerURI().startsWith("https://")) {
            swarmClusterConfiguration.setDockerCACert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CA_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            swarmClusterConfiguration.setDockerClientCert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            swarmClusterConfiguration.setDockerClientKey(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_KEY_NAME).toFile(), StandardCharsets.UTF_8));
        }

        return swarmClusterConfiguration;
    }
}
