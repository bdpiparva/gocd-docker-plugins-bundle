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

import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.*;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

class ShouldAssignWorkRequestExecutorTest extends BaseTest {
    @Mock
    private ElasticAgentRequestClient elasticAgentRequestClient;
    @Mock
    private ConsoleLogAppender consoleLogAppender;
    private DockerServices dockerServices;
    private DockerService instance;
    private final String environment = "production";
    private JobIdentifier jobIdentifier;
    private SwarmElasticProfileConfiguration properties;
    private ShouldAssignWorkRequestExecutor executor;
    private SwarmClusterConfiguration swarmClusterConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        initMocks(this);
        swarmClusterConfiguration = createClusterProfiles();
        jobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 876578L);
        dockerServices = new DockerServices();
        properties = new SwarmElasticProfileConfiguration();
        properties.setImage("alpine:latest");

        CreateAgentRequest createAgentRequest = new CreateAgentRequest();
        createAgentRequest.setAutoRegisterKey(UUID.randomUUID().toString())
                .setElasticProfileConfiguration(properties)
                .setEnvironment(environment)
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(swarmClusterConfiguration);

        instance = dockerServices.create(createAgentRequest, elasticAgentRequestClient, consoleLogAppender);
        services.add(instance.name());

        Map<String, DockerServices> clusterToServicesMap = new HashMap<>();
        clusterToServicesMap.put(swarmClusterConfiguration.uuid(), dockerServices);
        executor = new ShouldAssignWorkRequestExecutor(clusterToServicesMap);
    }

    @Test
    void shouldAssignWorkToContainerWithMatchingJobId() {
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest();
        request.setAgent(new Agent(instance.name(), null, null, null))
                .setEnvironment(environment)
                .setElasticProfileConfiguration(properties)
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(swarmClusterConfiguration);

        GoPluginApiResponse response = executor.execute(request);
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("true");
    }

    @Test
    void shouldNotAssignWorkToContainerWithNotMatchingJobId() {
        JobIdentifier mismatchingJobIdentifier = new JobIdentifier("up42", 98765L, "foo", "stage_1", "30000", "job_1", 999999L);
        ShouldAssignWorkRequest request = new ShouldAssignWorkRequest();
        request.setAgent(new Agent(instance.name(), null, null, null))
                .setEnvironment(environment)
                .setElasticProfileConfiguration(properties)
                .setClusterProfileProperties(swarmClusterConfiguration)
                .setJobIdentifier(mismatchingJobIdentifier);

        GoPluginApiResponse response = executor.execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("false");
    }
}
