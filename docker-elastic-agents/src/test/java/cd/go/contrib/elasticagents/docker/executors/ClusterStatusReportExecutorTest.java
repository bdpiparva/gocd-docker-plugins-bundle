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
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.StatusReport;
import cd.go.contrib.elasticagents.docker.requests.ClusterStatusReportRequest;
import cd.go.contrib.elasticagents.common.ViewBuilder;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ClusterStatusReportExecutorTest {
    private ClusterProfileProperties clusterProfile;
    @Mock
    private ViewBuilder viewBuilder;

    @Mock
    private DockerContainers dockerContainers;

    @Mock
    private Template template;
    private ClusterStatusReportExecutor executor;

    @BeforeEach
    void setUp() {
        initMocks(this);
        clusterProfile = new ClusterProfileProperties();
        Map<String, DockerContainers> clusterToContainersMap = new HashMap<>();
        clusterToContainersMap.put(clusterProfile.uuid(), dockerContainers);
        executor = new ClusterStatusReportExecutor(clusterToContainersMap, viewBuilder);
    }

    @Test
    void shouldGetStatusReport() throws Exception {
        StatusReport statusReport = aStatusReport();
        when(dockerContainers.getStatusReport(clusterProfile)).thenReturn(statusReport);
        when(viewBuilder.getTemplate("docker/cluster-status-report.template.ftlh")).thenReturn(template);
        when(viewBuilder.build(template, statusReport)).thenReturn("statusReportView");

        ClusterStatusReportRequest request = new ClusterStatusReportRequest()
                .setClusterProfile(clusterProfile);

        GoPluginApiResponse goPluginApiResponse = executor.execute(request);

        JsonObject expectedResponseBody = new JsonObject();
        expectedResponseBody.addProperty("view", "statusReportView");
        assertThat(goPluginApiResponse.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedResponseBody.toString(), goPluginApiResponse.responseBody(), true);
    }

    private StatusReport aStatusReport() {
        return new StatusReport("os", "x86_64", "0.1.2", 2, "100M", new ArrayList<>());
    }

}
