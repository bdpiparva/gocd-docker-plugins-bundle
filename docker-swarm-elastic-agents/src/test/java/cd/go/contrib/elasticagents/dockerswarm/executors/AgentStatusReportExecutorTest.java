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
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.requests.AgentStatusReportRequest;
import cd.go.contrib.elasticagents.dockerswarm.utils.JobIdentifierMother;
import com.google.gson.reflect.TypeToken;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.swarm.*;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.JOB_IDENTIFIER_LABEL_KEY;
import static cd.go.contrib.elasticagents.dockerswarm.utils.Util.GSON;
import static com.spotify.docker.client.DockerClient.LogsParam.stderr;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class AgentStatusReportExecutorTest {
    @Mock
    private AgentStatusReportRequest statusReportRequest;
    @Mock
    private DockerClientFactory dockerClientFactory;
    @Mock
    private DockerClient client;
    @Mock
    private SwarmClusterConfiguration swarmClusterConfiguration;

    private AgentStatusReportExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        initMocks(this);
        executor = new AgentStatusReportExecutor(dockerClientFactory, ViewBuilder.instance());
        swarmClusterConfiguration = new SwarmClusterConfiguration();
        when(dockerClientFactory.docker(swarmClusterConfiguration)).thenReturn(client);
        when(statusReportRequest.getClusterProfileConfiguration()).thenReturn(swarmClusterConfiguration);
    }

    @Test
    void shouldReturnAgentStatusReportBasedOnProvidedElasticAgentId() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(JobIdentifierMother.get());
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(of(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream("some-logs"));

        GoPluginApiResponse response = executor.execute(statusReportRequest);

        assertThat(response.responseCode()).isEqualTo(200);
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertThat(responseMap.containsKey("view")).isTrue();

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertServiceDetails(service, document);
        assertServiceLog(document, "some-logs");
        assertThat(hasEnvironmentVariable(document, "Foo", "Bar")).isTrue();
        assertThat(hasEnvironmentVariable(document, "Baz", null)).isTrue();
    }

    @Test
    void shouldNotPrintAutoRegisterKey() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(JobIdentifierMother.get());
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(of(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream("some-logs"));

        GoPluginApiResponse response = executor.execute(statusReportRequest);

        assertThat(response.responseCode()).isEqualTo(200);
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertThat(responseMap.containsKey("view")).isTrue();

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertServiceDetails(service, document);
        assertServiceLog(document, "some-logs");
        assertThat(hasEnvironmentVariable(document, "Foo", "Bar")).isTrue();
        assertThat(hasEnvironmentVariable(document, "Baz", null)).isTrue();
        assertThat(hasEnvironmentVariable(document, "GO_EA_AUTO_REGISTER_KEY", null)).isFalse();
    }

    @Test
    void shouldPrintMessageWhenLogIsNotAvailable() throws Exception {
        final Service service = mockedService("elastic-agent-id", "abcd-xyz");
        when(statusReportRequest.getJobIdentifier()).thenReturn(null);
        when(statusReportRequest.getElasticAgentId()).thenReturn("elastic-agent-id");
        when(client.listServices()).thenReturn(of(service));
        when(client.serviceLogs("abcd-xyz", stdout(), stderr())).thenReturn(new StubbedLogStream(""));

        GoPluginApiResponse response = executor.execute(statusReportRequest);

        assertThat(response.responseCode()).isEqualTo(200);
        final Map<String, String> responseMap = GSON.fromJson(response.responseBody(), new TypeToken<Map<String, String>>() {
        }.getType());
        assertThat(responseMap.containsKey("view")).isTrue();

        final Document document = Jsoup.parse(responseMap.get("view"));
        assertThat(document.select(".service-logs").text()).isEqualTo("Logs not available for this agent.");
    }

    private boolean hasEnvironmentVariable(Document document, String name, String value) {
        final Elements elements = document.select(MessageFormat.format(".environments .name-value .name-value_pair label:contains({0})", name));
        if (elements.isEmpty()) {
            return false;
        }

        final String envValueSpanText = StringUtils.stripToNull(elements.get(0).parent().select("span").text());
        return StringUtils.equals(value, envValueSpanText);
    }

    private void assertServiceLog(Document document, String logs) {
        final Elements logDetails = document.select(".service-logs").select("textarea");
        assertThat(logDetails.val()).isEqualTo(logs);
    }

    private void assertServiceDetails(Service service, Document document) {
        final Elements serviceDetails = document.select(".tab-content").attr("ng-show", "currenttab == 'service-details'");
        final String serviceDetailsText = serviceDetails.text();

        assertThat(serviceDetailsText).contains(service.id());
        assertThat(serviceDetailsText).contains(service.spec().name());
        assertThat(serviceDetailsText).contains(service.spec().taskTemplate().containerSpec().image());
    }

    private Service mockedService(String serviceName, String serviceId) {
        final ContainerSpec containerSpec = ContainerSpec.builder()
                .hosts(of("10.0.0.1 foo.bar.com", "10.0.0.1 abx.baz.com"))
                .hostname("some-hostname")
                .image("gocd/gocd-docker-agent:v18.2.0")
                .command("")
                .env("Foo=Bar", "Baz", "GO_EA_AUTO_REGISTER_KEY=auto-register-key")
                .args("")
                .build();

        final TaskSpec template = TaskSpec.builder()
                .containerSpec(containerSpec)
                .resources(ResourceRequirements.builder().build())
                .placement(Placement.create(of("")))
                .build();

        final ServiceSpec serviceSpec = ServiceSpec.builder()
                .addLabel(JOB_IDENTIFIER_LABEL_KEY, new JobIdentifier().toJson())
                .name(serviceName)
                .taskTemplate(template).build();

        return new StubbedService(serviceId, serviceSpec);
    }

    class StubbedLogStream implements LogStream {
        private final String logs;

        StubbedLogStream(String logs) {

            this.logs = logs;
        }

        @Override
        public String readFully() {
            return logs;
        }

        @Override
        public void attach(OutputStream stdout, OutputStream stderr) throws IOException {

        }

        @Override
        public void attach(OutputStream stdout, OutputStream stderr, boolean closeAtEof) throws IOException {

        }

        @Override
        public void close() {

        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public LogMessage next() {
            return null;
        }
    }

    class StubbedService extends Service {
        private final String serviceId;
        private final ServiceSpec serviceSpec;

        StubbedService(String serviceId, ServiceSpec serviceSpec) {
            this.serviceId = serviceId;
            this.serviceSpec = serviceSpec;
        }

        @Override
        public String id() {
            return serviceId;
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public Date createdAt() {
            return new Date();
        }

        @Override
        public Date updatedAt() {
            return new Date();
        }

        @Override
        public ServiceSpec spec() {
            return serviceSpec;
        }

        @Override
        public Endpoint endpoint() {
            return null;
        }

        @Override
        public UpdateStatus updateStatus() {
            return null;
        }
    }
}
