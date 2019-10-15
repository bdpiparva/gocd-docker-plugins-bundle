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
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.RegistryAuth;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerClientFactory {
    private DefaultDockerClient client;
    private SwarmClusterConfiguration swarmClusterConfiguration;

    private static final DockerClientFactory DOCKER_CLIENT_FACTORY = new DockerClientFactory();

    public synchronized DockerClient docker(SwarmClusterConfiguration swarmClusterConfiguration) throws Exception {
        if (swarmClusterConfiguration.equals(this.swarmClusterConfiguration) && this.client != null) {
            return this.client;
        }

        this.swarmClusterConfiguration = swarmClusterConfiguration;
        this.client = createClient(swarmClusterConfiguration);
        return this.client;
    }

    public static DockerClientFactory instance() {
        return DOCKER_CLIENT_FACTORY;
    }

    private static DefaultDockerClient createClient(SwarmClusterConfiguration swarmClusterConfiguration) throws Exception {
        DefaultDockerClient.Builder builder = DefaultDockerClient.builder();

        builder.uri(swarmClusterConfiguration.getDockerURI());
        if (swarmClusterConfiguration.getDockerURI().startsWith("https://")) {
            setupCerts(swarmClusterConfiguration, builder);
        }

        if (swarmClusterConfiguration.useDockerAuthInfo()) {
            final RegistryAuth registryAuth = swarmClusterConfiguration.registryAuth();
            DockerSwarmPlugin.LOG.info(format("Using private docker registry server `{0}`.", registryAuth.serverAddress()));
            builder.registryAuth(registryAuth);
        }

        DefaultDockerClient docker = builder.build();
        String ping = docker.ping();
        if (!"OK".equals(ping)) {
            throw new RuntimeException("Could not ping the docker server, the server said '" + ping + "' instead of 'OK'.");
        }
        return docker;
    }

    private static void setupCerts(SwarmClusterConfiguration swarmClusterConfiguration,
                                   DefaultDockerClient.Builder builder) throws IOException, DockerCertificateException {
        if (isBlank(swarmClusterConfiguration.getDockerCACert()) || isBlank(swarmClusterConfiguration.getDockerClientCert()) || isBlank(swarmClusterConfiguration.getDockerClientKey())) {
            DockerSwarmPlugin.LOG.warn("Missing docker certificates, will attempt to connect without certificates");
            return;
        }

        Path certificateDir = Files.createTempDirectory(UUID.randomUUID().toString());
        File tempDirectory = certificateDir.toFile();

        try {
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CA_CERT_NAME), swarmClusterConfiguration.getDockerCACert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_CERT_NAME), swarmClusterConfiguration.getDockerClientCert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_KEY_NAME), swarmClusterConfiguration.getDockerClientKey(), StandardCharsets.UTF_8);
            builder.dockerCertificates(new DockerCertificates(certificateDir));
        } finally {
            FileUtils.deleteDirectory(tempDirectory);
        }
    }
}
