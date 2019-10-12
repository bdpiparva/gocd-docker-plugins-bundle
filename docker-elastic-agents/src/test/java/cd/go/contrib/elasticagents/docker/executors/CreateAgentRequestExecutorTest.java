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

import cd.go.contrib.elasticagents.docker.ConsoleLogAppender;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class CreateAgentRequestExecutorTest {

    private ClusterProfileProperties clusterProfileProperties;
    private CreateAgentRequestExecutor executor;
    private ElasticProfileConfiguration elasticProfileConfiguration;

    @Mock
    private DockerContainers dockerContainers;
    @Mock
    private PluginRequest pluginRequest;

    @BeforeEach
    void setUp() {
        initMocks(this);
        elasticProfileConfiguration = new ElasticProfileConfiguration().setImage("image1");
        clusterProfileProperties = new ClusterProfileProperties();
        Map<String, DockerContainers> clusterToContainersMap = new HashMap<>();
        clusterToContainersMap.put(clusterProfileProperties.uuid(), dockerContainers);
        executor = new CreateAgentRequestExecutor(clusterToContainersMap, pluginRequest);
    }

    @Test
    void shouldAskDockerContainersToCreateAnAgent() throws Exception {
        final JobIdentifier jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);
        CreateAgentRequest request = new CreateAgentRequest()
                .setAutoRegisterKey("key1")
                .setElasticProfileConfiguration(elasticProfileConfiguration)
                .setEnvironment("env1")
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(clusterProfileProperties);

        executor.execute(request);

        verify(dockerContainers).create(eq(request), eq(pluginRequest), any(ConsoleLogAppender.class));
        verify(pluginRequest).appendToConsoleLog(eq(jobIdentifier), contains("Received request to create a container of image1 at "));
    }

    @Test
    void shouldLogErrorMessageToConsoleIfAgentCreateFails() throws Exception {
        final JobIdentifier jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);
        CreateAgentRequest request = new CreateAgentRequest()
                .setAutoRegisterKey("key1")
                .setElasticProfileConfiguration(elasticProfileConfiguration)
                .setEnvironment("env1")
                .setJobIdentifier(jobIdentifier)
                .setClusterProfileProperties(clusterProfileProperties);

        when(dockerContainers.create(eq(request), eq(pluginRequest), any(ConsoleLogAppender.class))).thenThrow(new RuntimeException("Ouch!"));

        try {
            executor.execute(request);
            fail("Should have thrown an exception");
        } catch (RuntimeException e) {
            // expected
        }

        verify(pluginRequest).appendToConsoleLog(eq(jobIdentifier), contains("Received request to create a container of image1 at "));
        verify(pluginRequest).appendToConsoleLog(eq(jobIdentifier), contains("Failed while creating container: Ouch"));
    }
}
