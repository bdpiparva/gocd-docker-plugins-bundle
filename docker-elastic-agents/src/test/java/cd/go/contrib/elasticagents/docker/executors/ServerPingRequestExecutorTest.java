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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.common.Clock;
import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.*;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.BaseTest;
import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.ServerPingRequest;
import org.joda.time.Period;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import static cd.go.contrib.elasticagents.common.agent.AgentConfigState.Disabled;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ServerPingRequestExecutorTest extends BaseTest {

    @Test
    void testShouldDisableIdleAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(of(new Agent(agentId, AgentState.Idle, AgentBuildState.Idle, AgentConfigState.Enabled)));
        DockerContainers agentInstances = new DockerContainers();

        ElasticAgentRequestClient pluginRequest = mock(ElasticAgentRequestClient.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.getAllClusterProfileConfigurations()).thenReturn(of(createClusterProfiles()));
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);

        final Collection<Agent> values = agents.agents();
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(dockerContainers, pluginRequest).execute(serverPingRequest);
        verify(pluginRequest).disableAgents(argThat(collectionMatches(values)));
    }

    private ArgumentMatcher<Collection<Agent>> collectionMatches(final Collection<Agent> values) {
        return new ArgumentMatcher<Collection<Agent>>() {
            @Override
            public boolean matches(Collection<Agent> argument) {
                return new ArrayList<>(argument).equals(new ArrayList<>(values));
            }
        };
    }

    @Test
    void testShouldTerminateDisabledAgents() throws Exception {
        String agentId = UUID.randomUUID().toString();
        final Agents agents = new Agents(of(new Agent(agentId, AgentState.Idle, AgentBuildState.Idle, Disabled)));
        DockerContainers agentInstances = new DockerContainers();

        ElasticAgentRequestClient pluginRequest = mock(ElasticAgentRequestClient.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.getAllClusterProfileConfigurations()).thenReturn(of(createClusterProfiles()));
        when(pluginRequest.listAgents()).thenReturn(agents);
        verifyNoMoreInteractions(pluginRequest);
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(dockerContainers, pluginRequest).execute(serverPingRequest);
        final Collection<Agent> values = agents.agents();
        verify(pluginRequest, atLeast(1)).deleteAgents(argThat(collectionMatches(values)));
    }

    @Test
    void testShouldTerminateInstancesThatNeverAutoRegistered() throws Exception {
        ElasticAgentRequestClient pluginRequest = mock(ElasticAgentRequestClient.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.getAllClusterProfileConfigurations()).thenReturn(of(createClusterProfiles()));
        when(pluginRequest.listAgents()).thenReturn(new Agents());
        verifyNoMoreInteractions(pluginRequest);

        DockerContainers agentInstances = new DockerContainers();
        agentInstances.clock = new Clock.TestClock().forward(Period.minutes(11));
        ElasticProfileConfiguration elasticProfileConfiguration = new ElasticProfileConfiguration()
                .setImage("alpine")
                .setCommand("/bin/sleep\n5");
        CreateAgentRequest request = new CreateAgentRequest();
        request.setElasticProfileConfiguration(elasticProfileConfiguration)
                .setJobIdentifier(new JobIdentifier())
                .setClusterProfileProperties(createClusterProfiles());
        DockerContainer container = agentInstances.create(request, pluginRequest, mock(ConsoleLogAppender.class));
        containers.add(container.name());

        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        new ServerPingRequestExecutor(dockerContainers, pluginRequest).execute(serverPingRequest);

        assertThat(agentInstances.hasInstance(container.name())).isFalse();
    }

    @Test
    void shouldDeleteAgentFromConfigWhenCorrespondingContainerIsNotPresent() throws Exception {
        ElasticAgentRequestClient pluginRequest = mock(ElasticAgentRequestClient.class);
        ServerPingRequest serverPingRequest = mock(ServerPingRequest.class);
        when(serverPingRequest.getAllClusterProfileConfigurations()).thenReturn(of(createClusterProfiles()));
        when(pluginRequest.listAgents()).thenReturn(new Agents(of(new Agent("foo", AgentState.Idle, AgentBuildState.Idle, AgentConfigState.Enabled))));
        verifyNoMoreInteractions(pluginRequest);

        DockerContainers agentInstances = new DockerContainers();
        HashMap<String, DockerContainers> dockerContainers = new HashMap<String, DockerContainers>() {{
            put(createClusterProfiles().uuid(), agentInstances);
        }};

        ServerPingRequestExecutor serverPingRequestExecutor = new ServerPingRequestExecutor(dockerContainers, pluginRequest);
        serverPingRequestExecutor.execute(serverPingRequest);
    }
}
