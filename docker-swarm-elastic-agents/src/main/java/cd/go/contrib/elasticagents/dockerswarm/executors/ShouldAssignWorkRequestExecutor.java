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

import cd.go.contrib.elasticagents.dockerswarm.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.requests.ShouldAssignWorkRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.text.MessageFormat.format;


public class ShouldAssignWorkRequestExecutor extends BaseExecutor<ShouldAssignWorkRequest> {

    public ShouldAssignWorkRequestExecutor(Map<String, DockerServices> clusterToServicesMap) {
        super(clusterToServicesMap);
    }


    @Override
    protected GoPluginApiResponse execute(ShouldAssignWorkRequest request) {
        try {
            refreshInstancesForCluster(request.getClusterProfileProperties());
            DockerServices dockerServices = clusterToServicesMap.get(request.getClusterProfileProperties().uuid());
            DockerService instance = dockerServices.find(request.getAgent().elasticAgentId());

            if (instance == null) {
                LOG.info(format("[should-assign-work] Agent with id `{0}` not exists.", request.getAgent().elasticAgentId()));
                return DefaultGoPluginApiResponse.success("false");
            }

            if (request.getJobIdentifier() != null && request.getJobIdentifier().getJobId().equals(instance.jobIdentifier().getJobId())) {
                LOG.info(format("[should-assign-work] Job with identifier {0} can be assigned to an agent {1}.", request.getJobIdentifier(), instance.name()));
                return DefaultGoPluginApiResponse.success("true");
            }

            LOG.info(format("[should-assign-work] Job with identifier {0} can not be assigned to an agent {1}.", request.getJobIdentifier(), instance.name()));
            return DefaultGoPluginApiResponse.success("false");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ShouldAssignWorkRequest parseRequest(String requestBody) {
        return fromJson(requestBody, ShouldAssignWorkRequest.class);
    }
}
