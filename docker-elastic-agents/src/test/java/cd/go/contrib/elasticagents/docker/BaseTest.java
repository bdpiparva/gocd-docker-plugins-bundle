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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;

import static java.lang.System.getenv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class BaseTest {

    private static DefaultDockerClient.Builder builder;
    static DefaultDockerClient docker;
    protected static HashSet<String> containers;

    @BeforeAll
    public static void beforeClass() throws Exception {
        builder = DefaultDockerClient.fromEnv();
        docker = builder.build();
        containers = new HashSet<>();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        for (String container : containers) {
            try {
                docker.inspectContainer(container);
                docker.stopContainer(container, 2);
                docker.removeContainer(container);
            } catch (ContainerNotFoundException ignore) {

            }
        }
    }

    protected ClusterProfileProperties createClusterProfiles() throws IOException {
        ClusterProfileProperties settings = new ClusterProfileProperties();

        settings.setMaxDockerContainers("1");
        settings.setDockerURI(builder.uri().toString());
        if (settings.getDockerURI().startsWith("https://")) {
            settings.setDockerCACert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CA_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            settings.setDockerClientCert(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_CERT_NAME).toFile(), StandardCharsets.UTF_8));
            settings.setDockerClientKey(FileUtils.readFileToString(Paths.get(getenv("DOCKER_CERT_PATH"), DockerCertificates.DEFAULT_CLIENT_KEY_NAME).toFile(), StandardCharsets.UTF_8));
        }

        return settings;
    }

    void assertContainerDoesNotExist(String id) throws DockerException, InterruptedException {
        try {
            docker.inspectContainer(id);
            fail("Expected ContainerNotFoundException");
        } catch (ContainerNotFoundException expected) {

        }
    }

    void assertContainerExist(String id) throws DockerException, InterruptedException {
        assertThat(docker.inspectContainer(id)).isNotNull();
    }

}
