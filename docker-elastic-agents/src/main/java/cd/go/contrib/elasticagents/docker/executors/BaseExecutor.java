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

import cd.go.contrib.elasticagents.docker.AgentInstances;
import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.plugin.base.executors.AbstractExecutor;

import java.util.List;
import java.util.Map;

public abstract class BaseExecutor<T> extends AbstractExecutor<T> {
    protected final Map<String, DockerContainers> clusterToContainersMap;

    BaseExecutor(Map<String, DockerContainers> clusterToContainersMap) {
        this.clusterToContainersMap = clusterToContainersMap;
    }

    protected void refreshInstancesForAllClusters(List<ClusterProfileProperties> listOfClusterProfileProperties) throws Exception {
        for (ClusterProfileProperties clusterProfileProperties : listOfClusterProfileProperties) {
            refreshInstancesForCluster(clusterProfileProperties);
        }
    }

    protected AgentInstances<DockerContainer> getAgentInstancesFor(ClusterProfileProperties clusterProfileProperties) {
        return clusterToContainersMap.get(clusterProfileProperties.uuid());
    }

    protected void refreshInstancesForCluster(ClusterProfileProperties clusterProfileProperties) throws Exception {
        DockerContainers dockerContainers = clusterToContainersMap.getOrDefault(clusterProfileProperties.uuid(), new DockerContainers());
        dockerContainers.refreshAll(clusterProfileProperties);
        clusterToContainersMap.put(clusterProfileProperties.uuid(), dockerContainers);
    }
}
