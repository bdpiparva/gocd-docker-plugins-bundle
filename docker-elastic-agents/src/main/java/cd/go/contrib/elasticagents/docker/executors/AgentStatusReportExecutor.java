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
import cd.go.contrib.elasticagents.docker.models.AgentStatusReport;
import cd.go.contrib.elasticagents.docker.models.ExceptionMessage;
import cd.go.contrib.elasticagents.docker.models.JobIdentifier;
import cd.go.contrib.elasticagents.docker.models.NotRunningAgentStatusReport;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.docker.views.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public class AgentStatusReportExecutor extends BaseExecutor<AgentStatusReportRequest> {
    private static final Logger LOG = Logger.getLoggerFor(AgentStatusReportExecutor.class);
    private final ViewBuilder viewBuilder;

    public AgentStatusReportExecutor(Map<String, DockerContainers> clusterToContainersMap, ViewBuilder viewBuilder) {
        super(clusterToContainersMap);
        this.viewBuilder = viewBuilder;
    }

    @Override
    protected GoPluginApiResponse execute(AgentStatusReportRequest request) {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));

        try {
            refreshInstancesForCluster(request.getClusterProfile());
            if (StringUtils.isNotBlank(elasticAgentId)) {
                return getStatusReportUsingElasticAgentId(request, elasticAgentId);
            }
            return getStatusReportUsingJobIdentifier(request, jobIdentifier);
        } catch (Exception e) {
            LOG.debug("Exception while generating agent status report", e);
            final String statusReportView;
            try {
                statusReportView = viewBuilder.build(viewBuilder.getTemplate("docker/error.template.ftlh"), new ExceptionMessage(e));
            } catch (IOException | TemplateException ex) {
                throw new RuntimeException(e);
            }
            return constructResponseForReport(statusReportView);
        }
    }

    private GoPluginApiResponse getStatusReportUsingJobIdentifier(AgentStatusReportRequest request,
                                                                  JobIdentifier jobIdentifier) throws Exception {
        DockerContainers dockerContainers = this.clusterToContainersMap.get(request.getClusterProfile().uuid());

        Optional<DockerContainer> dockerContainer = dockerContainers.find(jobIdentifier);
        if (dockerContainer.isPresent()) {
            AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(request.getClusterProfile(), dockerContainer.get());
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("docker/agent-status-report.template.ftlh"), agentStatusReport);
            return constructResponseForReport(statusReportView);
        }

        return containerNotFoundApiResponse(jobIdentifier);
    }

    private GoPluginApiResponse getStatusReportUsingElasticAgentId(AgentStatusReportRequest request,
                                                                   String elasticAgentId) throws Exception {
        DockerContainers dockerContainers = this.clusterToContainersMap.get(request.getClusterProfile().uuid());
        Optional<DockerContainer> dockerContainer = Optional.ofNullable(dockerContainers.find(elasticAgentId));
        if (dockerContainer.isPresent()) {
            AgentStatusReport agentStatusReport = dockerContainers.getAgentStatusReport(request.getClusterProfile(), dockerContainer.get());
            final String statusReportView = viewBuilder.build(viewBuilder.getTemplate("docker/agent-status-report.template.ftlh"), agentStatusReport);
            return constructResponseForReport(statusReportView);
        }
        return containerNotFoundApiResponse(elasticAgentId);
    }

    private GoPluginApiResponse constructResponseForReport(String statusReportView) {
        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("view", statusReportView);

        return DefaultGoPluginApiResponse.success(responseJSON.toString());
    }

    private GoPluginApiResponse containerNotFoundApiResponse(JobIdentifier jobIdentifier) throws IOException, TemplateException {
        Template template = viewBuilder.getTemplate("docker/not-running-agent-status-report.template.ftlh");
        final String statusReportView = viewBuilder.build(template, new NotRunningAgentStatusReport(jobIdentifier));
        return constructResponseForReport(statusReportView);
    }

    private GoPluginApiResponse containerNotFoundApiResponse(String elasticAgentId) throws IOException, TemplateException {
        Template template = viewBuilder.getTemplate("docker/not-running-agent-status-report.template.ftlh");
        final String statusReportView = viewBuilder.build(template, new NotRunningAgentStatusReport(elasticAgentId));
        return constructResponseForReport(statusReportView);
    }

    @Override
    protected AgentStatusReportRequest parseRequest(String requestBody) {
        return AgentStatusReportRequest.fromJSON(requestBody);
    }
}
