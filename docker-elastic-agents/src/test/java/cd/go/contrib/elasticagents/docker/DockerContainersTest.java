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

import cd.go.contrib.elasticagents.common.Agent;
import cd.go.contrib.elasticagents.common.Agents;
import cd.go.contrib.elasticagents.docker.models.*;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DockerContainersTest extends BaseTest {
    private CreateAgentRequest request;
    private DockerContainers dockerContainers;
    private ClusterProfileProperties clusterProfile;
    private final JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);
    private ConsoleLogAppender consoleLogAppender;

    @BeforeEach
    void setUp() throws Exception {
        clusterProfile = createClusterProfiles();
        request = new CreateAgentRequest()
                .setAutoRegisterKey("key")
                .setClusterProfileProperties(clusterProfile)
                .setJobIdentifier(jobIdentifier)
                .setEnvironment("production")
                .setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine").setCommand("/bin/sleep\n5"));
        dockerContainers = new DockerContainers();
        consoleLogAppender = mock(ConsoleLogAppender.class);
    }

    @Test
    void shouldCreateADockerInstance() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        containers.add(container.name());
        assertContainerExist(container.name());
    }

    @Test
    void shouldUpdateServerHealthMessageWithEmptyListWhileCreatingADockerInstance() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        containers.add(container.name());

        verify(pluginRequest).addServerHealthMessage(new ArrayList<>());
        assertContainerExist(container.name());
    }

    @Test
    void shouldTerminateAnExistingContainer() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        containers.add(container.name());

        dockerContainers.terminate(container.name(), clusterProfile);

        assertContainerDoesNotExist(container.name());
    }

    @Test
    void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerContainer container = DockerContainer.create(request, clusterProfile, docker, consoleLogAppender);
        containers.add(container.name());

        DockerContainers dockerContainers = new DockerContainers();

        ClusterProfileProperties profileProperties = createClusterProfiles();
        dockerContainers.refreshAll(profileProperties);
        assertThat(dockerContainers.find(container.name())).isEqualTo(container);
    }

    @Test
    void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerContainers dockerContainers = new DockerContainers();
        ClusterProfileProperties profileProperties = createClusterProfiles();
        dockerContainers.refreshAll(profileProperties);

        DockerContainer container = DockerContainer.create(request, clusterProfile, docker, consoleLogAppender);
        containers.add(container.name());

        dockerContainers.refreshAll(profileProperties);

        assertThat(dockerContainers.find(container.name())).isNull();
    }

    @Test
    void shouldNotListTheContainerIfItIsCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, clusterProfile, docker, consoleLogAppender);
        containers.add(container.name());

        ClusterProfileProperties profileProperties = createClusterProfiles();

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.refreshAll(profileProperties);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertThat(filteredDockerContainers.containsAgentWithId(container.name())).isFalse();
    }

    @Test
    void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerContainer container = DockerContainer.create(request, clusterProfile, docker, consoleLogAppender);
        containers.add(container.name());

        ClusterProfileProperties profileProperties = createClusterProfiles();

        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.refreshAll(profileProperties);

        Agents filteredDockerContainers = dockerContainers.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(Arrays.asList(new Agent(container.name(), null, null, null))));

        assertThat(filteredDockerContainers.containsAgentWithId(container.name())).isTrue();
    }

    @Test
    void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {
        // do not allow any containers
        clusterProfile.setMaxDockerContainers("0");

        PluginRequest pluginRequest = mock(PluginRequest.class);

        DockerContainer dockerContainer = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertThat(dockerContainer).isNull();

        // allow only one container
        clusterProfile.setMaxDockerContainers("1");
        dockerContainer = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertThat(dockerContainer).isNotNull();

        dockerContainer = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        if (dockerContainer != null) {
            containers.add(dockerContainer.name());
        }
        assertThat(dockerContainer).isNull();
    }

    @Test
    void shouldAddAWarningToTheServerHealthMessagesIfAgentsCannotBeCreated() throws Exception {
        // do not allow any containers
        clusterProfile.setMaxDockerContainers("0");

        PluginRequest pluginRequest = mock(PluginRequest.class);

        dockerContainers.create(request, pluginRequest, consoleLogAppender);

        CreateAgentRequest newRequest = new CreateAgentRequest()
                .setAutoRegisterKey("key")
                .setJobIdentifier(new JobIdentifier("up42", 2L, "foo", "stage", "1", "job2", 1L))
                .setClusterProfileProperties(clusterProfile)
                .setEnvironment("Production");

        dockerContainers.create(newRequest, pluginRequest, consoleLogAppender);
        ArrayList<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("type", "warning");
        message.put("message", "The number of containers currently running is currently at the maximum permissible limit, \"0\". Not creating more containers for jobs: up42/2/stage/1/job, up42/2/stage/1/job2.");
        messages.add(message);
        verify(pluginRequest).addServerHealthMessage(messages);
    }

    @Test
    void shouldTerminateUnregisteredContainersAfterTimeout() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);

        assertThat(dockerContainers.hasInstance(container.name())).isTrue();
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerContainers.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertThat(dockerContainers.hasInstance(container.name())).isFalse();
        assertContainerDoesNotExist(container.name());
    }

    @Test
    void shouldNotTerminateUnregistredContainersBeforeTimeout() throws Exception {
        DockerContainer container = dockerContainers.create(request, mock(PluginRequest.class), consoleLogAppender);
        containers.add(container.name());

        assertThat(dockerContainers.hasInstance(container.name())).isTrue();
        dockerContainers.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerContainers.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertThat(dockerContainers.hasInstance(container.name())).isTrue();
        assertContainerExist(container.name());
    }

    @Test
    void shouldGetStatusReport() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        containers.add(container.name());

        StatusReport statusReport = dockerContainers.getStatusReport(clusterProfile);

        assertThat(statusReport).isNotNull();
        assertThat(statusReport.getContainerStatusReports()).hasSize(1);
    }

    @Test
    void shouldGetAgentStatusReportUsingDockerContainer() throws Exception {
        PluginRequest pluginRequest = mock(PluginRequest.class);
        DockerContainer container = dockerContainers.create(request, pluginRequest, consoleLogAppender);
        containers.add(container.name());

        AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(clusterProfile, container);

        assertThat(agentStatusReport).isNotNull();
        assertThat(agentStatusReport.getElasticAgentId()).isEqualTo(container.name());
        assertThat(agentStatusReport.getJobIdentifier()).isEqualTo(request.getJobIdentifier());
    }
}
