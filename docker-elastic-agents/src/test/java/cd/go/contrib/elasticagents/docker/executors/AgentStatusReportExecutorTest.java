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
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.models.NotRunningAgentStatusReport;
import cd.go.contrib.elasticagents.docker.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.common.ViewBuilder;
import cd.go.contrib.elasticagents.common.JobIdentifier;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AgentStatusReportExecutorTest {
    @Mock
    private DockerContainers dockerContainers;
    @Mock
    private ViewBuilder viewBuilder;
    @Mock
    private Template template;
    private ClusterProfileProperties clusterProfileProperties;
    private AgentStatusReportExecutor agentStatusReportExecutor;

    @BeforeEach
    void setup() {
        initMocks(this);
        clusterProfileProperties = new ClusterProfileProperties().setDockerURI("https://ci.gocd.org/go");
        Map<String, DockerContainers> clusterToContainersMap = new HashMap<>();
        clusterToContainersMap.put(clusterProfileProperties.uuid(), dockerContainers);
        agentStatusReportExecutor = new AgentStatusReportExecutor(clusterToContainersMap, viewBuilder);
    }

    @Test
    void shouldGetAgentStatusReportWithElasticAgentId() throws Exception {
        String agentId = "elastic-agent-id";
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest()
                .setElasticAgentId(agentId)
                .setClusterProfile(clusterProfileProperties);
        AgentStatusReport agentStatusReport = new AgentStatusReport(null, agentId, null, null, null, null, null, new HashMap<>(), new ArrayList<>());

        DockerContainer dockerContainer = new DockerContainer("id", "name", new JobIdentifier(), new Date(), new ElasticProfileConfiguration(), null);
        when(dockerContainers.find(agentId)).thenReturn(dockerContainer);
        when(dockerContainers.getAgentStatusReport(clusterProfileProperties, dockerContainer)).thenReturn(agentStatusReport);
        when(viewBuilder.getTemplate("docker/agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, agentStatusReport)).thenReturn("agentStatusReportView");

        GoPluginApiResponse goPluginApiResponse = agentStatusReportExecutor.execute(agentStatusReportRequest);

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "agentStatusReportView");
        assertThat(goPluginApiResponse.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    void shouldGetAgentStatusReportWithJobIdentifier() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest()
                .setClusterProfile(clusterProfileProperties)
                .setJobIdentifier(jobIdentifier);

        AgentStatusReport agentStatusReport = new AgentStatusReport(jobIdentifier, "elastic-agent-id", null, null, null, null, null, new HashMap<>(), new ArrayList<>());

        DockerContainer dockerContainer = new DockerContainer("id", "name", jobIdentifier, new Date(), new ElasticProfileConfiguration(), null);
        when(dockerContainers.find(jobIdentifier)).thenReturn(Optional.of(dockerContainer));
        when(dockerContainers.getAgentStatusReport(clusterProfileProperties, dockerContainer)).thenReturn(agentStatusReport);
        when(viewBuilder.getTemplate("docker/agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, agentStatusReport)).thenReturn("agentStatusReportView");

        GoPluginApiResponse goPluginApiResponse = agentStatusReportExecutor.execute(agentStatusReportRequest);

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "agentStatusReportView");
        assertThat(goPluginApiResponse.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedJobIdentifier() throws Exception {
        JobIdentifier jobIdentifier = new JobIdentifier("up42", 2L, "label", "stage1", "1", "job", 1L);

        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest()
                .setClusterProfile(clusterProfileProperties)
                .setJobIdentifier(jobIdentifier);

        when(dockerContainers.find(jobIdentifier)).thenReturn(Optional.empty());
        when(viewBuilder.getTemplate("docker/not-running-agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(eq(template), any(NotRunningAgentStatusReport.class))).thenReturn("errorView");

        GoPluginApiResponse goPluginApiResponse = agentStatusReportExecutor.execute(agentStatusReportRequest);

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "errorView");
        assertThat(goPluginApiResponse.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    @Test
    void shouldRenderContainerNotFoundAgentStatusReportViewWhenNoContainerIsRunningForProvidedElasticAgentId() throws Exception {
        String elasticAgentId = "elastic-agent-id";
        AgentStatusReportRequest agentStatusReportRequest = new AgentStatusReportRequest()
                .setElasticAgentId(elasticAgentId)
                .setClusterProfile(clusterProfileProperties);

        when(dockerContainers.find(elasticAgentId)).thenReturn(null);
        when(viewBuilder.getTemplate("docker/not-running-agent-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(eq(template), any(NotRunningAgentStatusReport.class))).thenReturn("errorView");

        GoPluginApiResponse goPluginApiResponse = agentStatusReportExecutor.execute(agentStatusReportRequest);

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "errorView");
        assertThat(goPluginApiResponse.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }
}
