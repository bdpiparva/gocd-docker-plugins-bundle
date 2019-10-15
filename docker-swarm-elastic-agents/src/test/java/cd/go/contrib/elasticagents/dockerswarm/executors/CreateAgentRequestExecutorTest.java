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
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CreateAgentRequestExecutorTest {
    @Test
    void shouldAskDockerContainersToCreateAnAgent() throws Exception {
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        CreateAgentRequest request = new CreateAgentRequest();
        request.setClusterProfileProperties(clusterProfileProperties)
                .setElasticProfileConfiguration(new ElasticProfileConfiguration());

        ElasticAgentRequestClient pluginRequest = mock(ElasticAgentRequestClient.class);

        DockerServices dockerServices = mock(DockerServices.class);
        Map<String, DockerServices> clusterToServicesMap = new HashMap<>();
        clusterToServicesMap.put(clusterProfileProperties.uuid(), dockerServices);

        new CreateAgentRequestExecutor(clusterToServicesMap, pluginRequest).execute(request);

        verify(dockerServices).refreshAll(clusterProfileProperties);
        verify(dockerServices).create(eq(request), eq(pluginRequest), any(ConsoleLogAppender.class));

    }
}
