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
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.model.reports.SwarmCluster;
import cd.go.contrib.elasticagents.dockerswarm.requests.ClusterStatusReportRequest;
import com.spotify.docker.client.DockerClient;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import freemarker.template.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClusterStatusReportExecutorTest {
    private DockerClientFactory dockerClientFactory;
    private DockerClient dockerClient;
    private ClusterStatusReportRequest request;
    private SwarmClusterConfiguration swarmClusterConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        dockerClientFactory = mock(DockerClientFactory.class);
        dockerClient = mock(DockerClient.class);
        request = mock(ClusterStatusReportRequest.class);
        swarmClusterConfiguration = new SwarmClusterConfiguration();
        when(request.getClusterProfileConfiguration()).thenReturn(swarmClusterConfiguration);
        when(dockerClientFactory.docker(swarmClusterConfiguration)).thenReturn(dockerClient);
    }

    @Test
    void shouldBuildStatusReportView() throws Exception {
        final ViewBuilder builder = mock(ViewBuilder.class);
        final Template template = mock(Template.class);

        when(builder.getTemplate("docker-swarm/cluster-status-report.template.ftlh")).thenReturn(template);
        when(builder.build(eq(template), any(SwarmCluster.class))).thenReturn("status-report");
        final GoPluginApiResponse response = new ClusterStatusReportExecutor(dockerClientFactory, builder).execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{\"view\":\"status-report\"}");
    }
}
