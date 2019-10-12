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

import cd.go.contrib.elasticagents.docker.*;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ShouldAssignWorkRequestExecutorTest extends BaseTest {

    private Map<String, DockerContainers> clusterToContainersMap;
    private DockerContainer instance;
    private final String environment = "production";
    private final JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 1L);
    private ClusterProfileProperties clusterProfileProperties;

    @BeforeEach
    void setUp() throws Exception {
        clusterProfileProperties = createClusterProfiles();
        PluginRequest pluginRequest = mock(PluginRequest.class);
        CreateAgentRequest request = new CreateAgentRequest()
                .setAutoRegisterKey(UUID.randomUUID().toString())
                .setJobIdentifier(jobIdentifier)
                .setEnvironment(environment)
                .setClusterProfileProperties(clusterProfileProperties)
                .setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine").setCommand("/bin/sleep\n5"));
        DockerContainers agentInstances = new DockerContainers();
        instance = agentInstances.create(request, pluginRequest, mock(ConsoleLogAppender.class));
        clusterToContainersMap = new HashMap<>();
        clusterToContainersMap.put(clusterProfileProperties.uuid(), agentInstances);
        containers.add(instance.name());
    }

    @Test
    void shouldAssignWorkToContainerWithSameJobIdentifier() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest()
                .setAgent(new Agent(instance.name(), null, null, null))
                .setEnvironment(environment)
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(clusterToContainersMap).execute(request);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("true");
    }

    @Test
    void shouldNotAssignWorkToContainerWithDifferentJobIdentifier() {
        JobIdentifier otherJobId = new JobIdentifier("up42", 2L, "foo", "stage", "1", "job", 2L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest()
                .setAgent(new Agent(instance.name(), null, null, null))
                .setEnvironment(environment)
                .setJobIdentifier(otherJobId)
                .setClusterProfileProperties(clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(clusterToContainersMap).execute(request);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("false");
    }

    @Test
    void shouldNotAssignWorkIfInstanceIsNotFound() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest()
                .setAgent(new Agent("unknown-name", null, null, null))
                .setEnvironment(environment)
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(clusterProfileProperties);
        GoPluginApiResponse response = new ShouldAssignWorkRequestExecutor(clusterToContainersMap).execute(request);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("false");
    }
}
