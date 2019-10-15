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

import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.plugin.base.executors.AbstractExecutor;

import java.util.List;
import java.util.Map;

public abstract class BaseExecutor<T> extends AbstractExecutor<T> {
    protected final Map<String, DockerServices> clusterToServicesMap;

    BaseExecutor(Map<String, DockerServices> clusterToServicesMap) {
        this.clusterToServicesMap = clusterToServicesMap;
    }

    protected void refreshInstancesForAllClusters(List<SwarmClusterConfiguration> listOfDockerSwarmClusterProfileProperties) throws Exception {
        for (SwarmClusterConfiguration swarmClusterConfiguration : listOfDockerSwarmClusterProfileProperties) {
            refreshInstancesForCluster(swarmClusterConfiguration);
        }
    }

    protected DockerServices getAgentInstancesFor(
            SwarmClusterConfiguration swarmClusterConfiguration) {
        return clusterToServicesMap.get(swarmClusterConfiguration.uuid());
    }

    protected void refreshInstancesForCluster(SwarmClusterConfiguration swarmClusterConfiguration) throws Exception {
        DockerServices dockerContainers = clusterToServicesMap.getOrDefault(swarmClusterConfiguration.uuid(), new DockerServices());
        dockerContainers.refreshAll(swarmClusterConfiguration);
        clusterToServicesMap.put(swarmClusterConfiguration.uuid(), dockerContainers);
    }
}
