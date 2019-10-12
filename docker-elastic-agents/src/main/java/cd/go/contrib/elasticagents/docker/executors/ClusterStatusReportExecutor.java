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

import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.DockerPlugin;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.requests.ClusterStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import java.util.Map;

public class ClusterStatusReportExecutor extends BaseExecutor<ClusterStatusReportRequest> {
    private final ViewBuilder viewBuilder;

    public ClusterStatusReportExecutor(Map<String, DockerContainers> clusterToContainersMap,
                                       ViewBuilder viewBuilder) {
        super(clusterToContainersMap);
        this.viewBuilder = viewBuilder;
    }

    @Override
    protected GoPluginApiResponse execute(ClusterStatusReportRequest request) {
        try {
            refreshInstancesForCluster(request.getClusterProfile());
            DockerPlugin.LOG.info("[status-report] Generating status report");
            DockerContainers dockerContainers = this.clusterToContainersMap.get(request.getClusterProfile().uuid());
            StatusReport statusReport = dockerContainers.getStatusReport(request.getClusterProfile());

            final Template template = viewBuilder.getTemplate("docker/cluster-status-report.template.ftlh");
            final String statusReportView = viewBuilder.build(template, statusReport);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ClusterStatusReportRequest parseRequest(String requestBody) {
        return ClusterStatusReportRequest.fromJSON(requestBody);
    }
}
