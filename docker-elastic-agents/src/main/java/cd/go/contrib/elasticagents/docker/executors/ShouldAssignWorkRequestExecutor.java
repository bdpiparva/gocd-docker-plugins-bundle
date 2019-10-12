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

import cd.go.contrib.elasticagents.docker.DockerContainer;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Map;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;

public class ShouldAssignWorkRequestExecutor extends BaseExecutor<ShouldAssignWorkRequest> {

    public ShouldAssignWorkRequestExecutor(Map<String, DockerContainers> clusterToContainersMap) {
        super(clusterToContainersMap);
    }

    @Override
    protected GoPluginApiResponse execute(ShouldAssignWorkRequest request) {
        try {
            refreshInstancesForCluster(request.getClusterProfileProperties());
            LOG.info(String.format("[Should Assign Work] Processing should assign work request for job %s and agent %s", request.getJobIdentifier(), request.getAgent()));

            DockerContainers dockerContainers = clusterToContainersMap.get(request.getClusterProfileProperties().uuid());
            DockerContainer instance = dockerContainers.find(request.getAgent().elasticAgentId());

            if (instance == null) {
                return DefaultGoPluginApiResponse.success("false");
            }

            if (instance.getJobIdentifier().equals(request.getJobIdentifier())) {
                return DefaultGoPluginApiResponse.success("true");
            }

            return DefaultGoPluginApiResponse.success("false");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ShouldAssignWorkRequest parseRequest(String requestBody) {
        return ShouldAssignWorkRequest.fromJSON(requestBody);
    }
}
