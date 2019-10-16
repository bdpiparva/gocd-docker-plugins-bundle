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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.MockitoAnnotations.initMocks;

class JobCompletionRequestExecutorTest {
    private static final String ELASTIC_AGENT_ID = "agent-1";
    private Map<String, DockerServices> clusterToDockerServiceMap;

    @Mock
    private DockerServices mockAgentInstances;
    @Mock
    private ElasticAgentRequestClient mockPluginRequest;

    @Captor
    private ArgumentCaptor<List<Agent>> agentsArgumentCaptor;
    private SwarmClusterConfiguration swarmClusterConfiguration;
    private JobCompletionRequestExecutor executor;
    private JobCompletionRequest request;

    @BeforeEach
    void setUp() {
        initMocks(this);
        swarmClusterConfiguration = new SwarmClusterConfiguration();

        request = new JobCompletionRequest();
        request.setElasticAgentId(ELASTIC_AGENT_ID)
                .setJobIdentifier(new JobIdentifier(100L))
                .setClusterProfileConfiguration(swarmClusterConfiguration);

        clusterToDockerServiceMap = new HashMap<>();
        clusterToDockerServiceMap.put(swarmClusterConfiguration.uuid(), mockAgentInstances);
        executor = new JobCompletionRequestExecutor(clusterToDockerServiceMap, mockPluginRequest);
    }

    @Test
    void shouldTerminateElasticAgentOnJobCompletion() throws Exception {
        GoPluginApiResponse response = executor.execute(request);

        InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);
        inOrder.verify(mockPluginRequest).disableAgents(agentsArgumentCaptor.capture());
        inOrder.verify(mockAgentInstances).terminate(ELASTIC_AGENT_ID, swarmClusterConfiguration);
        inOrder.verify(mockPluginRequest).deleteAgents(agentsArgumentCaptor.capture());

        List<Agent> agentsToDisabled = agentsArgumentCaptor.getValue();
        List<Agent> agentsToDelete = agentsArgumentCaptor.getValue();

        assertThat(1).isEqualTo(agentsToDisabled.size());
        assertThat(ELASTIC_AGENT_ID).isEqualTo(agentsToDisabled.get(0).elasticAgentId());
        assertThat(agentsToDisabled).isEqualTo(agentsToDelete);
        assertThat(200).isEqualTo(response.responseCode());
        assertTrue(response.responseBody().isEmpty());
    }
}
