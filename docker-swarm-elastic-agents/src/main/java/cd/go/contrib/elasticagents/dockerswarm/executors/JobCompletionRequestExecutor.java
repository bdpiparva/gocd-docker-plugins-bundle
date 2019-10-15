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
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.text.MessageFormat.format;

public class JobCompletionRequestExecutor extends BaseExecutor<JobCompletionRequest> {
    private final ElasticAgentRequestClient pluginRequest;

    public JobCompletionRequestExecutor(Map<String, DockerServices> clusterToDockerServiceMap,
                                        ElasticAgentRequestClient pluginRequest) {
        super(clusterToDockerServiceMap);
        this.pluginRequest = pluginRequest;
    }

    @Override
    protected GoPluginApiResponse execute(JobCompletionRequest request) {
        try {
            refreshInstancesForCluster(request.getClusterProfileConfiguration());
            SwarmClusterConfiguration swarmClusterConfiguration = request.getClusterProfileConfiguration();
            String elasticAgentId = request.getElasticAgentId();

            Agent agent = new Agent();
            agent.elasticAgentId(elasticAgentId);

            LOG.info(format("[Job Completion] Terminating elastic agent with id {0} on job completion {1}.", agent.elasticAgentId(), request.getJobIdentifier()));

            DockerServices dockerServices = clusterToServicesMap.get(swarmClusterConfiguration.uuid());
            List<Agent> agents = Collections.singletonList(agent);
            pluginRequest.disableAgents(agents);
            dockerServices.terminate(agent.elasticAgentId(), swarmClusterConfiguration);
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
