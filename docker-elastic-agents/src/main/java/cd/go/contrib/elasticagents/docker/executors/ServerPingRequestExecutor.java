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

import cd.go.contrib.elasticagents.common.Agent;
import cd.go.contrib.elasticagents.common.Agents;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.PluginRequest;
import cd.go.contrib.elasticagents.common.ServerRequestFailedException;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.requests.ServerPingRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class ServerPingRequestExecutor extends BaseExecutor<ServerPingRequest> {
    private final PluginRequest pluginRequest;

    public ServerPingRequestExecutor(Map<String, DockerContainers> clusterToContainersMap,
                                     PluginRequest pluginRequest) {
        super(clusterToContainersMap);
        this.pluginRequest = pluginRequest;
    }

    @Override
    protected GoPluginApiResponse execute(ServerPingRequest request) {
        //todo: remove possiblyMissingAgents, refer to ecs/kubernetes server ping implementation
        try {
            refreshInstancesForAllClusters(request.getAllClusterProfileProperties());
            Set<Agent> possiblyMissingAgents = new HashSet<>();
            List<ClusterProfileProperties> allClusterProfileProperties = request.getAllClusterProfileProperties();

            for (ClusterProfileProperties clusterProfileProperties : allClusterProfileProperties) {
                performCleanupForACluster(clusterProfileProperties, clusterToContainersMap.get(clusterProfileProperties.uuid()), possiblyMissingAgents);
            }

            refreshInstancesAgainToCheckForPossiblyMissingAgents(allClusterProfileProperties, possiblyMissingAgents);
            return DefaultGoPluginApiResponse.success("");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void performCleanupForACluster(ClusterProfileProperties clusterProfileProperties,
                                           DockerContainers dockerContainers,
                                           Set<Agent> possiblyMissingAgents) throws Exception {
        Agents allAgents = pluginRequest.listAgents();

        for (Agent agent : allAgents.agents()) {
            if (dockerContainers.find(agent.elasticAgentId()) == null) {
                possiblyMissingAgents.add(agent);
            } else {
                possiblyMissingAgents.remove(agent);
            }
        }

        Agents agentsToDisable = dockerContainers.instancesCreatedAfterTimeout(clusterProfileProperties, allAgents);
        disableIdleAgents(agentsToDisable);

        allAgents = pluginRequest.listAgents();
        terminateDisabledAgents(allAgents, clusterProfileProperties, dockerContainers);

        dockerContainers.terminateUnregisteredInstances(clusterProfileProperties, allAgents);
    }

    private void refreshInstancesAgainToCheckForPossiblyMissingAgents(List<ClusterProfileProperties> allClusterProfileProperties,
                                                                      Set<Agent> possiblyMissingAgents) throws Exception {
        DockerContainers dockerContainers = new DockerContainers();
        for (ClusterProfileProperties clusterProfileProperties : allClusterProfileProperties) {
            dockerContainers.refreshAll(clusterProfileProperties);
        }

        Agents missingAgents = new Agents();
        for (Agent possiblyMissingAgent : possiblyMissingAgents) {
            if (!dockerContainers.hasInstance(possiblyMissingAgent.elasticAgentId())) {
                LOG.warn("[Server Ping] Was expecting a container with name " + possiblyMissingAgent.elasticAgentId() + ", but it was missing!");
                missingAgents.add(possiblyMissingAgent);
            }
        }

        pluginRequest.disableAgents(missingAgents.agents());
        pluginRequest.deleteAgents(missingAgents.agents());
    }

    private void disableIdleAgents(Agents agents) throws ServerRequestFailedException {
        pluginRequest.disableAgents(agents.findInstancesToDisable());
    }

    private void terminateDisabledAgents(Agents agents,
                                         ClusterProfileProperties clusterProfileProperties,
                                         DockerContainers dockerContainers) throws Exception {
        Collection<Agent> toBeDeleted = agents.findInstancesToTerminate();

        for (Agent agent : toBeDeleted) {
            dockerContainers.terminate(agent.elasticAgentId(), clusterProfileProperties);
        }

        pluginRequest.deleteAgents(toBeDeleted);
    }

    @Override
    protected ServerPingRequest parseRequest(String requestBody) {
        return ServerPingRequest.fromJSON(requestBody);
    }
}
