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

import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.common.requests.AbstractCreateAgentRequest;
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ContainerStatusReport;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class DockerContainerTest extends BaseTest {
    private AbstractCreateAgentRequest<ElasticProfileConfiguration, ClusterProfileProperties> request;
    private JobIdentifier jobIdentifier;
    private ConsoleLogAppender consoleLogAppender;

    @BeforeEach
    void setUp() {
        jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);

        request = new CreateAgentRequest()
                .setAutoRegisterKey("key")
                .setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine").setCommand("/bin/sleep\n5"))
                .setEnvironment("production")
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(new ClusterProfileProperties());
        consoleLogAppender = mock(ConsoleLogAppender.class);
    }

    @Test
    void shouldCreateContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    void shouldPullAnImageWhenOneDoesNotExist() throws Exception {
        String imageName = request.getElasticProfileConfiguration().getImage();

        try {
            docker.removeImage(imageName, true, false);
        } catch (ImageNotFoundException ignore) {
        }
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        assertThat(docker.inspectImage(imageName)).isNotNull();
        assertContainerExist(container.name());
    }

    @Test
    void shouldRaiseExceptionWhenImageIsNotFoundInDockerRegistry() throws Exception {
        String imageName = "ubuntu:does-not-exist";
        request.getElasticProfileConfiguration().setImage(imageName);

        assertThatCode(() -> DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender))
                .isInstanceOf(ImageNotFoundException.class)
                .hasMessage("Image not found: " + imageName);
    }

    @Test
    void shouldNotCreateContainerIfTheImageIsNotProvided() throws Exception {
        request.getElasticProfileConfiguration().setImage(null);

        assertThatCode(() -> DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must provide `Image` attribute.");
    }

    @Test
    void shouldStartContainerWithCorrectEnvironmentVariables() throws Exception {
        request.getElasticProfileConfiguration().setEnvironmentVariables("A=B\nC=D\r\nE=F\n\n\nX=Y");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        clusterProfiles.setEnvironmentVariables("GLOBAL=something");
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.config().env()).contains("A=B", "C=D", "E=F", "X=Y", "GLOBAL=something");
        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(containerInfo);
    }

    @Test
    void shouldStartContainerWithPrivilegedMode() throws Exception {
        request.getElasticProfileConfiguration().setPrivileged("true");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        clusterProfiles.setEnvironmentVariables("GLOBAL=something");
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().privileged()).isTrue();
    }

    @Test
    void shouldStartContainerWithAutoregisterEnvironmentVariables() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());
        assertThat(containerInfo.config().env()).contains("GO_EA_AUTO_REGISTER_KEY=key");
        assertThat(containerInfo.config().env()).contains("GO_EA_AUTO_REGISTER_ENVIRONMENT=production");
        assertThat(containerInfo.config().env()).contains("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + container.name());
        assertThat(containerInfo.config().env()).contains("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID);
    }

    @Test
    void shouldStartContainerWithCorrectCommand() throws Exception {
        request.getElasticProfileConfiguration().setCommand("cat\n/etc/hosts\n/etc/group");

        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());
        assertThat(containerInfo.config().cmd()).isEqualTo(Arrays.asList("cat", "/etc/hosts", "/etc/group"));
        String logs = docker.logs(container.name(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs).contains("127.0.0.1"); // from /etc/hosts
        assertThat(logs).contains("floppy:x:"); // from /etc/group
    }

    @Test
    void shouldTerminateAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        container.terminate(docker);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    void shouldFindAnExistingContainer() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        DockerContainer dockerContainer = DockerContainer.fromContainerInfo(docker.inspectContainer(container.name()));

        assertThat(dockerContainer).isEqualTo(container);
    }

    @Test
    void shouldStartContainerWithHostEntry() throws Exception {
        request.getElasticProfileConfiguration()
                .setHosts("127.0.0.2\tbaz \n192.168.5.1\tfoo\tbar\n127.0.0.1  gocd.local")
                .setCommand("cat\n/etc/hosts");

        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);

        containers.add(container.name());
        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        final ImmutableList<String> extraHosts = containerInfo.hostConfig().extraHosts();
        assertThat(extraHosts).contains("baz:127.0.0.2", "foo\tbar:192.168.5.1", "gocd.local:127.0.0.1");

        String logs = docker.logs(container.name(), DockerClient.LogsParam.stdout()).readFully();
        assertThat(logs).contains("127.0.0.2\tbaz");
        assertThat(logs).contains("192.168.5.1\tfoo");
        assertThat(logs).contains("127.0.0.1\tgocd.local");

        AgentStatusReport agentStatusReport = container.getAgentStatusReport(docker);
        assertThat(agentStatusReport.getHosts()).contains("baz:127.0.0.2", "foo\tbar:192.168.5.1", "gocd.local:127.0.0.1");
    }

    @Test
    void shouldGetContainerStatusReport() throws Exception {
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        ContainerStatusReport containerStatusReport = container.getContainerStatusReport(docker);

        assertThat(containerStatusReport).isNotNull();
        assertThat(containerStatusReport.getElasticAgentId()).isEqualTo(container.name());
        assertThat(containerStatusReport.getImage()).isEqualTo("alpine:latest");
        assertThat(containerStatusReport.getJobIdentifier()).isEqualTo(jobIdentifier);
        assertThat(containerStatusReport.getState()).isEqualToIgnoringCase("running");
    }

    @Test
    void shouldGetAgentStatusReport() throws Exception {
        request.getElasticProfileConfiguration().setEnvironmentVariables("A=B\nC=D");
        DockerContainer container = DockerContainer.create(request, createClusterProfiles(), docker, consoleLogAppender);
        containers.add(container.name());

        AgentStatusReport agentStatusReport = container.getAgentStatusReport(docker);

        assertThat(agentStatusReport).isNotNull();
        assertThat(agentStatusReport.getElasticAgentId()).isEqualTo(container.name());
        assertThat(agentStatusReport.getImage()).isEqualTo("alpine:latest");
        assertThat(agentStatusReport.getJobIdentifier()).isEqualTo(jobIdentifier);
        assertThat(agentStatusReport.getCommand()).isEqualTo("/bin/sleep");
        Map<String, String> environmentVariables = agentStatusReport.getEnvironmentVariables();
        assertThat(environmentVariables).containsEntry("A", "B");
        assertThat(environmentVariables).containsEntry("C", "D");
    }

    @Test
    void shouldPullImageWhenPullSettingIsEnabled() throws Exception {
        String imageName = request.getElasticProfileConfiguration().getImage();
        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        clusterProfiles.setPullOnContainerCreate(true);

        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        assertContainerExist(container.name());
        containers.add(container.name());

        assertThat(docker.inspectImage(imageName)).isNotNull();
        assertContainerExist(container.name());
    }

    @Test
    void shouldStartContainerWithMemoryLimits() throws Exception {
        request.getElasticProfileConfiguration()
                .setMaxMemory("10M")
                .setReservedMemory("6M");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().memoryReservation()).isEqualTo(6 * 1024 * 1024L);
        assertThat(containerInfo.hostConfig().memory()).isEqualTo(10 * 1024 * 1024L);
    }

    @Test
    void shouldStartContainerNoMemoryLimits() throws Exception {
        request.getElasticProfileConfiguration()
                .setMaxMemory("")
                .setReservedMemory("");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().memoryReservation()).isEqualTo(0L);
        assertThat(containerInfo.hostConfig().memory()).isEqualTo(0L);
    }

    @Test
    void shouldStartContainerWithCpuLimit() throws Exception {
        request.getElasticProfileConfiguration().setCpus(".75");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().cpuPeriod()).isEqualTo(100_000L);
        assertThat(containerInfo.hostConfig().cpuQuota()).isEqualTo(75_000L);
    }

    @Test
    void shouldStartContainerWithMountedVolumes() throws Exception {
        // using "/" as source folder because it seems the folder must exist on testing machine
        request.getElasticProfileConfiguration().setMounts("/:/A\n/:/B:ro");

        ClusterProfileProperties clusterProfiles = createClusterProfiles();
        DockerContainer container = DockerContainer.create(request, clusterProfiles, docker, consoleLogAppender);
        containers.add(container.name());

        ContainerInfo containerInfo = docker.inspectContainer(container.name());

        assertThat(containerInfo.hostConfig().binds()).contains("/:/A", "/:/B:ro");
    }
}
