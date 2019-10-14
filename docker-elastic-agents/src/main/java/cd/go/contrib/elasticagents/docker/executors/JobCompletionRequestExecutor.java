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

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;

public class JobCompletionRequestExecutor extends BaseExecutor<JobCompletionRequest> {
    private final ElasticAgentRequestClient pluginRequest;

    public JobCompletionRequestExecutor(Map<String, DockerContainers> clusterToContainersMap,
                                        ElasticAgentRequestClient pluginRequest) {
        super(clusterToContainersMap);
        this.pluginRequest = pluginRequest;
    }

    @Override
    protected GoPluginApiResponse execute(JobCompletionRequest request) {
        try {
            refreshInstancesForCluster(request.getClusterProfileConfiguration());
            DockerContainers dockerContainers = this.clusterToContainersMap.get(request.getClusterProfileConfiguration().uuid());
            ClusterProfileProperties clusterProfileProperties = request.getClusterProfileConfiguration();
            String elasticAgentId = request.getElasticAgentId();
            Agent agent = new Agent(elasticAgentId);
            LOG.info("[Job Completion] Terminating elastic agent with id {} on job completion {} in cluster {}.", agent.elasticAgentId(), request.getJobIdentifier(), clusterProfileProperties);
            List<Agent> agents = List.of(agent);
            pluginRequest.disableAgents(agents);
            dockerContainers.terminate(agent.elasticAgentId(), clusterProfileProperties);

            pluginRequest.deleteAgents(agents);
            return DefaultGoPluginApiResponse.success("");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected JobCompletionRequest parseRequest(String requestBody) {
        return fromJson(requestBody, JobCompletionRequest.class);
    }
}
