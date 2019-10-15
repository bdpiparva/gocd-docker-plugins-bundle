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

import cd.go.contrib.elasticagents.common.Clock;
import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.Agents;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import org.joda.time.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DockerServicesTest extends BaseTest {
    private CreateAgentRequest request;
    private DockerServices dockerServices;
    private SwarmClusterConfiguration swarmClusterConfiguration;
    private JobIdentifier jobIdentifier;
    private ElasticAgentRequestClient pluginRequest;
    private ConsoleLogAppender consoleLogAppender;

    @BeforeEach
    void setUp() throws Exception {
        consoleLogAppender = mock(ConsoleLogAppender.class);
        pluginRequest = mock(ElasticAgentRequestClient.class);
        jobIdentifier = new JobIdentifier(100L);
        swarmClusterConfiguration = createClusterProfiles();

        SwarmElasticProfileConfiguration elasticAgentProperties = new SwarmElasticProfileConfiguration();
        elasticAgentProperties.setImage("alpine:latest");
        elasticAgentProperties.setCommand("/bin/sleep\n5");

        request = new CreateAgentRequest();
        request.setAutoRegisterKey("key")
                .setElasticProfileConfiguration(elasticAgentProperties)
                .setEnvironment("production")
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(swarmClusterConfiguration);

        dockerServices = new DockerServices();
    }

    @Test
    void shouldCreateADockerInstance() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        services.add(dockerService.name());
        assertServiceExist(dockerService.name());
    }

    @Test
    void shouldTerminateAnExistingContainer() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        services.add(dockerService.name());

        dockerServices.terminate(dockerService.name(), swarmClusterConfiguration);

        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    void shouldRefreshAllAgentInstancesAtStartUp() throws Exception {
        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        DockerServices dockerServices = new DockerServices();
        dockerServices.refreshAll(swarmClusterConfiguration);
        assertThat(dockerServices.find(dockerService.name())).isEqualTo(dockerService);
    }

    @Test
    void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerServices dockerServices = new DockerServices();
        dockerServices.refreshAll(swarmClusterConfiguration);

        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        dockerServices.refreshAll(swarmClusterConfiguration);

        assertThat(dockerServices.find(dockerService.name())).isNull();
    }


    @Test
    void shouldNotRefreshAllAgentInstancesAgainAfterTheStartUpIfForceRefreshIsFalse() throws Exception {
        DockerServices dockerServices = new DockerServices();
        dockerServices.refreshAll(swarmClusterConfiguration);

        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        dockerServices.refreshAll(swarmClusterConfiguration, false);

        assertThat(dockerServices.find(dockerService.name())).isNull();
    }

    @Test
    void shouldRefreshAllAgentInstancesAgainAfterTheStartUp() throws Exception {
        DockerServices dockerServices = new DockerServices();
        dockerServices.refreshAll(swarmClusterConfiguration, true);

        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        dockerServices.refreshAll(swarmClusterConfiguration, true);

        assertThat(dockerServices.find(dockerService.name())).isEqualTo(dockerService);
    }

    @Test
    void shouldNotListTheServiceIfItIsCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.refreshAll(swarmClusterConfiguration);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(of(new Agent(dockerService.name(), null, null, null))));

        assertThat(filteredDockerContainers.containsAgentWithId(dockerService.name())).isFalse();
    }

    @Test
    void shouldListTheContainerIfItIsNotCreatedBeforeTimeout() throws Exception {
        DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker);
        services.add(dockerService.name());

        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.refreshAll(swarmClusterConfiguration);

        Agents filteredDockerContainers = dockerServices.instancesCreatedAfterTimeout(createClusterProfiles(), new Agents(of(new Agent(dockerService.name(), null, null, null))));

        assertThat(filteredDockerContainers.containsAgentWithId(dockerService.name())).isTrue();
    }

    @Test
    void shouldNotCreateContainersIfMaxLimitIsReached() throws Exception {
        SwarmElasticProfileConfiguration elasticAgentProperties = new SwarmElasticProfileConfiguration();
        elasticAgentProperties.setImage("alpine:latest");
        swarmClusterConfiguration.setMaxDockerContainers("0");

        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertThat(dockerService).isNull();

        // allow only one container
        swarmClusterConfiguration.setMaxDockerContainers("1");
        dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertThat(dockerService).isNotNull();

        dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        if (dockerService != null) {
            services.add(dockerService.name());
        }
        assertThat(dockerService).isNull();
    }

    @Test
    void shouldTerminateUnregistredContainersAfterTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);

        assertThat(dockerServices.hasInstance(dockerService.name())).isTrue();
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(11));
        dockerServices.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertThat(dockerServices.hasInstance(dockerService.name())).isFalse();
        assertServiceDoesNotExist(dockerService.name());
    }

    @Test
    void shouldNotTerminateUnregistredServiceBeforeTimeout() throws Exception {
        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        services.add(dockerService.name());

        assertThat(dockerServices.hasInstance(dockerService.name())).isTrue();
        dockerServices.clock = new Clock.TestClock().forward(Period.minutes(9));
        dockerServices.terminateUnregisteredInstances(createClusterProfiles(), new Agents());
        assertThat(dockerServices.hasInstance(dockerService.name())).isTrue();
        assertServiceExist(dockerService.name());
    }

    @Test
    void shouldAddServerHealthMessagesIfMaxContainerLimitIsReached() throws Exception {
        SwarmElasticProfileConfiguration elasticAgentProperties = new SwarmElasticProfileConfiguration();
        elasticAgentProperties.setImage("alpine:latest");
        request.setElasticProfileConfiguration(elasticAgentProperties);

        swarmClusterConfiguration.setMaxDockerContainers("0");
        DockerService dockerService = dockerServices.create(request, pluginRequest, consoleLogAppender);
        assertThat(dockerService).isNull();
        ArrayList<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("type", "warning");
        message.put("message", "The number of containers currently running is currently at the maximum permissible limit (0). Not creating any more containers.");
        messages.add(message);
        verify(pluginRequest).addServerHealthMessage(messages);
    }
}
