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
import cd.go.contrib.elasticagents.dockerswarm.*;
import cd.go.contrib.elasticagents.dockerswarm.requests.JobCompletionRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collections;
import java.util.List;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static java.text.MessageFormat.format;

public class JobCompletionRequestExecutor implements RequestExecutor {
    private final JobCompletionRequest jobCompletionRequest;
    private final AgentInstances<DockerService> agentInstances;
    private final ElasticAgentRequestClient pluginRequest;

    public JobCompletionRequestExecutor(JobCompletionRequest jobCompletionRequest,
                                        AgentInstances<DockerService> agentInstances,
                                        ElasticAgentRequestClient pluginRequest) {
        this.jobCompletionRequest = jobCompletionRequest;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        ClusterProfileProperties pluginSettings = jobCompletionRequest.getClusterProfileProperties();
        String elasticAgentId = jobCompletionRequest.getElasticAgentId();

        Agent agent = new Agent();
        agent.elasticAgentId(elasticAgentId);

        LOG.info(format("[Job Completion] Terminating elastic agent with id {0} on job completion {1}.", agent.elasticAgentId(), jobCompletionRequest.jobIdentifier()));

        List<Agent> agents = Collections.singletonList(agent);
        pluginRequest.disableAgents(agents);
        agentInstances.terminate(agent.elasticAgentId(), pluginSettings);
        pluginRequest.deleteAgents(agents);
        return DefaultGoPluginApiResponse.success("");
    }
}
