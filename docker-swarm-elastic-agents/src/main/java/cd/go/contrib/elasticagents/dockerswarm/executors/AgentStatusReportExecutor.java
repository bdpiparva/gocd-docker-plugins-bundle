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

import cd.go.contrib.elasticagents.common.ViewBuilder;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.Constants;
import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.model.reports.agent.DockerServiceElasticAgent;
import cd.go.contrib.elasticagents.dockerswarm.reports.StatusReportGenerationErrorHandler;
import cd.go.contrib.elasticagents.dockerswarm.reports.StatusReportGenerationException;
import cd.go.contrib.elasticagents.dockerswarm.requests.AgentStatusReportRequest;
import cd.go.plugin.base.executors.AbstractExecutor;
import com.google.gson.JsonObject;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.swarm.Service;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;

public class AgentStatusReportExecutor extends AbstractExecutor<AgentStatusReportRequest> {
    private final DockerClientFactory dockerClientFactory;
    private final ViewBuilder builder;

    public AgentStatusReportExecutor() {
        this(DockerClientFactory.instance(), ViewBuilder.instance());
    }

    public AgentStatusReportExecutor(DockerClientFactory dockerClientFactory,
                                     ViewBuilder builder) {
        this.dockerClientFactory = dockerClientFactory;
        this.builder = builder;
    }


    @Override
    protected AgentStatusReportRequest parseRequest(String requestBody) {
        return AgentStatusReportRequest.fromJSON(requestBody, AgentStatusReportRequest.class);
    }

    @Override
    protected GoPluginApiResponse execute(AgentStatusReportRequest request) {
        String elasticAgentId = request.getElasticAgentId();
        JobIdentifier jobIdentifier = request.getJobIdentifier();
        LOG.info(String.format("[status-report] Generating status report for agent: %s with job: %s", elasticAgentId, jobIdentifier));

        try {
            final DockerClient dockerClient = dockerClientFactory.docker(request.getClusterProfileConfiguration());
            Service dockerService = findService(elasticAgentId, jobIdentifier, dockerClient);

            DockerServiceElasticAgent elasticAgent = DockerServiceElasticAgent.fromService(dockerService, dockerClient);
            final String statusReportView = builder.build(builder.getTemplate("/docker-swarm/agent-status-report.template.ftlh"), elasticAgent);

            JsonObject responseJSON = new JsonObject();
            responseJSON.addProperty("view", statusReportView);

            return DefaultGoPluginApiResponse.success(responseJSON.toString());
        } catch (Exception e) {
            return StatusReportGenerationErrorHandler.handle(builder, e);
        }
    }

    private Service findService(String elasticAgentId,
                                JobIdentifier jobIdentifier,
                                DockerClient dockerClient) throws Exception {
        Service dockerService;
        if (StringUtils.isNotBlank(elasticAgentId)) {
            dockerService = findServiceUsingElasticAgentId(elasticAgentId, dockerClient);
        } else {
            dockerService = findServiceUsingJobIdentifier(jobIdentifier, dockerClient);
        }
        return dockerService;
    }

    private Service findServiceUsingJobIdentifier(JobIdentifier jobIdentifier, DockerClient client) {
        try {
            return client.listServices(Service.Criteria.builder().addLabel(Constants.JOB_IDENTIFIER_LABEL_KEY, jobIdentifier.toJson()).build()).get(0);
        } catch (Exception e) {
            throw StatusReportGenerationException.noRunningService(jobIdentifier);
        }
    }

    private Service findServiceUsingElasticAgentId(String elasticAgentId, DockerClient client) throws Exception {
        for (Service service : client.listServices()) {
            if (Objects.equals(service.spec().name(), elasticAgentId) || service.id().equals(elasticAgentId)) {
                return service;
            }
        }
        throw StatusReportGenerationException.noRunningService(elasticAgentId);
    }
}
