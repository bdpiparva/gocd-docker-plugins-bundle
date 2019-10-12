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

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerClientFactory {

    private static DefaultDockerClient client;
    private static ClusterProfileProperties properties;

    public static synchronized DockerClient docker(ClusterProfileProperties properties) throws Exception {
        if (properties.equals(DockerClientFactory.properties) && DockerClientFactory.client != null) {
            return DockerClientFactory.client;
        }

        DockerClientFactory.properties = properties;
        DockerClientFactory.client = createClient(properties);
        return DockerClientFactory.client;
    }

    private static DefaultDockerClient createClient(ClusterProfileProperties clusterProfile) throws Exception {
        DefaultDockerClient.Builder builder = DefaultDockerClient.builder();

        builder.uri(clusterProfile.getDockerURI());
        if (clusterProfile.getDockerURI().startsWith("https://")) {
            setupCerts(clusterProfile, builder);
        }

        if (clusterProfile.useDockerAuthInfo()) {
            RegistryAuth auth;
            if (clusterProfile.useCustomRegistryCredentials()) {
                auth = RegistryAuth.builder()
                        .password(clusterProfile.getPrivateRegistryPassword())
                        .serverAddress(clusterProfile.getPrivateRegistryServer())
                        .username(clusterProfile.getPrivateRegistryUsername())
                        .build();
            } else {
                auth = RegistryAuth.fromDockerConfig(clusterProfile.getPrivateRegistryServer()).build();
            }
            builder.registryAuth(auth);
        }

        DefaultDockerClient docker = builder.build();
        String ping = docker.ping();
        if (!"OK".equals(ping)) {
            throw new RuntimeException("Could not ping the docker server, the server said '" + ping + "' instead of 'OK'.");
        }
        return docker;
    }

    private static void setupCerts(ClusterProfileProperties clusterProfile,
                                   DefaultDockerClient.Builder builder) throws IOException, DockerCertificateException {
        if (isBlank(clusterProfile.getDockerCACert()) || isBlank(clusterProfile.getDockerClientCert()) || isBlank(clusterProfile.getDockerClientKey())) {
            LOG.warn("Missing docker certificates, will attempt to connect without certificates");
            return;
        }

        Path certificateDir = Files.createTempDirectory(UUID.randomUUID().toString());
        File tempDirectory = certificateDir.toFile();

        try {
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CA_CERT_NAME), clusterProfile.getDockerCACert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_CERT_NAME), clusterProfile.getDockerClientCert(), StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(tempDirectory, DockerCertificates.DEFAULT_CLIENT_KEY_NAME), clusterProfile.getDockerClientKey(), StandardCharsets.UTF_8);
            builder.dockerCertificates(new DockerCertificates(certificateDir));
        } finally {
            FileUtils.deleteDirectory(tempDirectory);
        }
    }
}
