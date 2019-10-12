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

import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.builders.PluginStatusReportViewBuilder;
import cd.go.contrib.elasticagents.dockerswarm.model.reports.SwarmCluster;
import cd.go.contrib.elasticagents.dockerswarm.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagents.dockerswarm.requests.ClusterStatusReportRequest;
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;

import java.io.IOException;

import static cd.go.contrib.elasticagents.dockerswarm.DockerPlugin.LOG;

public class ClusterStatusReportExecutor {

    private final ClusterStatusReportRequest clusterStatusReportRequest;
    private final DockerServices agentInstances;
    private final DockerClientFactory dockerClientFactory;
    private PluginStatusReportViewBuilder viewBuilder;

    public ClusterStatusReportExecutor(ClusterStatusReportRequest clusterStatusReportRequest, DockerServices agentInstances) throws IOException {
        this(clusterStatusReportRequest, agentInstances, DockerClientFactory.instance(), PluginStatusReportViewBuilder.instance());
    }

    ClusterStatusReportExecutor(ClusterStatusReportRequest clusterStatusReportRequest, DockerServices agentInstances, DockerClientFactory dockerClientFactory, PluginStatusReportViewBuilder viewBuilder) {
        this.clusterStatusReportRequest = clusterStatusReportRequest;
        this.agentInstances = agentInstances;
        this.dockerClientFactory = dockerClientFactory;
        this.viewBuilder = viewBuilder;
    }

    public GoPluginApiResponse execute() {
        try {
            LOG.debug("[status-report] Generating cluster status report.");
            final DockerClient dockerClient = dockerClientFactory.docker(clusterStatusReportRequest.getClusterProfile());
            final SwarmCluster swarmCluster = new SwarmCluster(dockerClient);
            final Template template = viewBuilder.getTemplate("status-report.template.ftlh");
            final String statusReportView = viewBuilder.build(template, swarmCluster);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(viewBuilder, e);
        }
    }
}
