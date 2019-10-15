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

import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.lang.String.format;
import static java.util.List.of;
import static org.apache.commons.lang.StringUtils.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DockerServiceElasticAgentTest extends BaseTest {
    private CreateAgentRequest createAgentRequest;

    private JobIdentifier jobIdentifier;

    @BeforeEach
    void setUp() {
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();

        ElasticProfileConfiguration elasticAgentProperties = new ElasticProfileConfiguration();
        elasticAgentProperties.setImage("alpine:latest");

        jobIdentifier = new JobIdentifier(100L);
        createAgentRequest = new CreateAgentRequest();
        createAgentRequest.setAutoRegisterKey("key")
                .setElasticProfileConfiguration(elasticAgentProperties)
                .setEnvironment("production")
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(clusterProfileProperties);
    }

    @Test
    void shouldCreateService() throws Exception {
        DockerService dockerService = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    void shouldCreateServiceForTheJobId() throws Exception {
        DockerService dockerService = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertThat(dockerService.jobIdentifier()).isEqualTo(jobIdentifier);
    }

    @Test
    void shouldNotCreateServiceIfTheImageIsNotProvided() {
        createAgentRequest.setClusterProfileProperties(new ClusterProfileProperties())
                .setElasticProfileConfiguration(new ElasticProfileConfiguration());

        assertThatCode(() -> DockerService.create(createAgentRequest, createClusterProfiles(), docker))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must provide `Image` attribute.");
    }

    @Test
    void shouldStartServiceWithCorrectLabel() throws Exception {
        DockerService dockerService = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());

        Service serviceInfo = docker.inspectService(dockerService.name());
        ImmutableMap<String, String> labels = serviceInfo.spec().labels();

        assertThat(labels.get(Constants.JOB_IDENTIFIER_LABEL_KEY)).isEqualTo(jobIdentifier.toJson());
        assertThat(labels.get(Constants.ENVIRONMENT_LABEL_KEY)).isEqualTo("production");
        assertThat(labels.get(Constants.CREATED_BY_LABEL_KEY)).isEqualTo(Constants.PLUGIN_ID);
        assertThat(labels.get(Constants.CONFIGURATION_LABEL_KEY)).isEqualTo(new Gson().toJson(createAgentRequest.getElasticProfileConfiguration()));
    }

    @Test
    void shouldStartServiceWithCorrectEnvironment() throws Exception {
        createAgentRequest.getElasticProfileConfiguration().setEnvironment("A=B\nC=D\r\nE=F\n\n\nX=Y");
        createAgentRequest.getClusterProfileProperties().setEnvironmentVariables("GLOBAL=something");

        DockerService service = DockerService.create(createAgentRequest, createAgentRequest.getClusterProfileProperties(), docker);
        services.add(service.name());

        Service serviceInfo = docker.inspectService(service.name());

        DockerService dockerService = DockerService.fromService(serviceInfo);
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env())
                .contains("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something");
        assertThat(dockerService.getElasticProfileConfiguration().getEnvironment())
                .isEqualTo(createAgentRequest.getElasticProfileConfiguration().getEnvironment());
    }

    @Test
    void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        ElasticProfileConfiguration elasticProfileConfiguration = new ElasticProfileConfiguration();
        elasticProfileConfiguration.setImage("alpine:latest");

        createAgentRequest.setElasticProfileConfiguration(elasticProfileConfiguration)
                .setClusterProfileProperties(createClusterProfiles());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().containerSpec().env())
                .contains(
                        "GO_EA_AUTO_REGISTER_KEY=key",
                        "GO_EA_AUTO_REGISTER_ENVIRONMENT=production",
                        "GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + service.name(),
                        "GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID
                );
    }

    @Test
    void shouldStartContainerWithCorrectCommand() throws Exception {
        ElasticProfileConfiguration properties = new ElasticProfileConfiguration();
        properties.setImage("alpine:latest");
        properties.setCommand(join(of("/bin/sh", "-c", "cat /etc/hosts /etc/group"), "\n"));

        createAgentRequest.setElasticProfileConfiguration(properties)
                .setClusterProfileProperties(new ClusterProfileProperties());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());

        assertThat(serviceInfo.spec().taskTemplate().containerSpec().command()).isEqualTo(Arrays.asList("/bin/sh", "-c", "cat /etc/hosts /etc/group"));
    }

    @Test
    void shouldStartContainerWithCorrectMemoryLimit() throws Exception {
        ElasticProfileConfiguration properties = new ElasticProfileConfiguration();
        properties.setImage("alpine:latest");
        properties.setMaxMemory("512MB");
        properties.setReservedMemory("100MB");

        createAgentRequest.setElasticProfileConfiguration(properties)
                .setEnvironment("prod")
                .setClusterProfileProperties(new ClusterProfileProperties());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());
        Service serviceInfo = docker.inspectService(service.name());
        assertThat(serviceInfo.spec().taskTemplate().resources().limits().memoryBytes()).isEqualTo(512 * 1024 * 1024L);
        assertThat(serviceInfo.spec().taskTemplate().resources().reservations().memoryBytes()).isEqualTo(100 * 1024 * 1024L);
    }

    @Test
    void shouldStartContainerWithHostEntry() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Swarm host entry support");

        ElasticProfileConfiguration elasticProfileConfiguration = new ElasticProfileConfiguration();
        elasticProfileConfiguration.setImage("alpine:latest");
        elasticProfileConfiguration.setHosts("127.0.0.1 foo bar\n 127.0.0.2 baz");

        createAgentRequest.setElasticProfileConfiguration(elasticProfileConfiguration)
                .setClusterProfileProperties(new ClusterProfileProperties())
                .setEnvironment("prod");

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectServiceInfo = docker.inspectService(service.name());
        assertThat(inspectServiceInfo.spec().taskTemplate().containerSpec().hosts()).containsExactly("127.0.0.1 foo", "127.0.0.1 bar", "127.0.0.2 baz");
    }

    @Test
    void shouldStartContainerWithMountedVolume() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Docker volume mount.");
        final String volumeName = UUID.randomUUID().toString();

        ElasticProfileConfiguration properties = new ElasticProfileConfiguration();
        properties.setImage("alpine:latest");
        properties.setMounts("source=" + volumeName + ", target=/path/in/container");

        createAgentRequest.setElasticProfileConfiguration(properties)
                .setEnvironment("prod")
                .setClusterProfileProperties(new ClusterProfileProperties());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectServiceInfo = docker.inspectService(service.name());
        final Mount mount = inspectServiceInfo.spec().taskTemplate().containerSpec().mounts().get(0);

        assertThat(mount.source()).isEqualTo(volumeName);
        assertThat(mount.type()).isEqualTo("volume");
    }

    @Test
    void shouldTerminateAnExistingService() throws Exception {
        DockerService dockerService = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(dockerService.name());

        dockerService.terminate(docker);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    void shouldFindAnExistingService() throws Exception {
        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());

        DockerService dockerService = DockerService.fromService(docker.inspectService(service.name()));

        assertThat(dockerService).isEqualTo(service);
    }

    @Test
    void shouldFindAnExistingServiceWithJobIdInformation() throws Exception {
        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());
        assertThat(service.jobIdentifier()).isEqualTo(jobIdentifier);

        DockerService dockerService = DockerService.fromService(docker.inspectService(service.name()));

        assertThat(service.jobIdentifier()).isEqualTo(jobIdentifier);
        assertThat(dockerService).isEqualTo(service);
    }

    @Test
    void shouldStartContainerWithSecret() throws Exception {
        requireDockerApiVersionAtLeast("1.26", "Swarm secret support");

        final String secretName = UUID.randomUUID().toString();
        final SecretCreateResponse secret = docker.createSecret(SecretSpec.builder()
                .name(secretName)
                .data(Base64.getEncoder().encodeToString("some-random-junk".getBytes()))
                .labels(Collections.singletonMap("cd.go.contrib.elasticagents.dockerswarm.elasticagent.DockerSwarmPlugin", ""))
                .build()
        );

        final List<String> command = Arrays.asList("/bin/sh", "-c", "cat /run/secrets/" + secretName);
        ElasticProfileConfiguration properties = new ElasticProfileConfiguration();
        properties.setImage("alpine:latest");
        properties.setSecrets("src=" + secretName);
        properties.setCommand(join(command, "\n"));

        createAgentRequest.setElasticProfileConfiguration(properties)
                .setEnvironment("prod")
                .setClusterProfileProperties(new ClusterProfileProperties());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectService = docker.inspectService(service.name());
        final SecretBind secretBind = inspectService.spec().taskTemplate().containerSpec().secrets().get(0);

        assertThat(secretBind.secretName()).isEqualTo(secretName);
        assertThat(secretBind.secretId()).isEqualTo(secret.id());
    }

    @Test
    void shouldCreateServiceWithConstraints() throws Exception {
        final List<Node> nodes = docker.listNodes();
        final String nodeId = nodes.get(0).id();
        final ElasticProfileConfiguration properties = new ElasticProfileConfiguration();
        properties.setImage("alpine:latest");
        properties.setConstraints(format("node.id == %s", nodeId));

        createAgentRequest.setElasticProfileConfiguration(properties)
                .setEnvironment("prod")
                .setClusterProfileProperties(new ClusterProfileProperties());

        DockerService service = DockerService.create(createAgentRequest, createClusterProfiles(), docker);
        services.add(service.name());

        final Service inspectService = docker.inspectService(service.name());
        final ImmutableList<String> constraints = inspectService.spec().taskTemplate().placement().constraints();

        assertThat(constraints).containsExactly(format("node.id == %s", nodeId));
    }
}
