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
import cd.go.contrib.elasticagents.dockerswarm.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobCompletionRequestExecutorTest {
    @Mock
    private ElasticAgentRequestClient mockPluginRequest;
    @Mock
    private AgentInstances<DockerService> mockAgentInstances;

    @Captor
    private ArgumentCaptor<List<Agent>> agentsArgumentCaptor;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldTerminateElasticAgentOnJobCompletion() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier(100L);
        ClusterProfileProperties profileProperties = new ClusterProfileProperties();
        String elasticAgentId = "agent-1";
        JobCompletionRequest request = new JobCompletionRequest(elasticAgentId, jobIdentifier, profileProperties);
        JobCompletionRequestExecutor executor = new JobCompletionRequestExecutor(request, mockAgentInstances, mockPluginRequest);


        GoPluginApiResponse response = executor.execute();

        InOrder inOrder = inOrder(mockPluginRequest, mockAgentInstances);

        inOrder.verify(mockPluginRequest).disableAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDisabled = agentsArgumentCaptor.getValue();
        assertEquals(1, agentsToDisabled.size());
        assertEquals(elasticAgentId, agentsToDisabled.get(0).elasticAgentId());
        inOrder.verify(mockAgentInstances).terminate(elasticAgentId, profileProperties);
        inOrder.verify(mockPluginRequest).deleteAgents(agentsArgumentCaptor.capture());
        List<Agent> agentsToDelete = agentsArgumentCaptor.getValue();

        assertEquals(agentsToDisabled, agentsToDelete);

        assertEquals(200, response.responseCode());
        assertTrue(response.responseBody().isEmpty());
    }
}
