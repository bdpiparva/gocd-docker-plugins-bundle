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

package cd.go.contrib.elasticagents.dockerswarm;

import cd.go.plugin.base.metadata.MetadataExtractor;
import cd.go.plugin.base.metadata.MetadataHolder;
import cd.go.plugin.base.test_helper.annotations.JsonSource;
import com.google.common.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Base64;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.PLUGIN_IDENTIFIER;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static cd.go.plugin.base.GsonTransformer.toJson;
import static cd.go.plugin.base.ResourceReader.readResource;
import static cd.go.plugin.base.ResourceReader.readResourceBytes;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DockerSwarmPluginTest {
    private DockerSwarmPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new DockerSwarmPlugin();
        plugin.initializeGoApplicationAccessor(mock(GoApplicationAccessor.class));
    }

    @Test
    void shouldReturnPluginIcon() throws UnhandledRequestTypeException {
        GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-icon"));

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(fromJSON(response.responseBody()))
                .containsEntry("data", Base64.getEncoder().encodeToString(readResourceBytes("/docker-swarm/icon.png")))
                .containsEntry("content_type", "image/png");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/docker-swarm/capabilities.json")
    void shouldReturnCapabilities(String expectedJson) throws UnhandledRequestTypeException, JSONException {
        GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-capabilities"));

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJson, response.responseBody(), true);
    }

    @Nested
    class ClusterProfileView {
        @Test
        void shouldRenderTheTemplateInJSON() throws Exception {
            GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-cluster-profile-view"));

            assertThat(response.responseCode()).isEqualTo(200);

            Map<String, String> hashSet = fromJSON(response.responseBody());
            assertThat(hashSet)
                    .containsEntry("template", readResource("/docker-swarm/cluster-profile.template.html"));
        }

        @Test
        void allFieldsShouldBePresentInView() {
            String template = readResource("/docker-swarm/cluster-profile.template.html");
            Document document = Jsoup.parse(template);

            for (MetadataHolder field : new MetadataExtractor().forClass(SwarmClusterConfiguration.class)) {
                assertThat(getInputOrTextArea(document, field.getKey()))
                        .describedAs(format("Field %s must be present", field.getKey()))
                        .isNotNull();
                assertThat(document.selectFirst(format("span[ng-show=\"GOINPUTNAME[%s].$error.server\"]", field.getKey())))
                        .isNotNull()
                        .satisfies(span -> assertThat(span.text())
                                .isEqualTo(format("{{GOINPUTNAME[%s].$error.server}}", field.getKey()))
                        );
            }
        }
    }

    @Nested
    class ClusterProfileMetadata {
        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/cluster-profile-metadata.json")
        void shouldSerializeAllFields(String metadataJson) throws Exception {
            GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-cluster-profile-metadata"));

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(metadataJson, response.responseBody(), true);
        }
    }

    @Nested
    class ClusterProfileValidation {
        private DefaultGoPluginApiRequest request;

        @BeforeEach
        void setUp() {
            request = request("cd.go.elastic-agent.validate-cluster-profile");
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/blank-cluster-profile-validation-error.json")
        void shouldValidateABadConfiguration(String expectedJSON) throws Exception {
            request.setRequestBody("{}");
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
        }

        @Test
        void shouldValidateAGoodConfiguration() throws Exception {
            SwarmClusterConfiguration swarmClusterConfiguration = new SwarmClusterConfiguration()
                    .setMaxDockerContainers("1")
                    .setDockerURI("https://api.example.com")
                    .setDockerClientCert("some ca cert")
                    .setDockerClientKey("some client key")
                    .setDockerClientCert("some client cert")
                    .setGoServerUrl("https://ci.example.com/go")
                    .setAutoRegisterTimeout("10")
                    .setUseDockerAuthInfo("false");

            request.setRequestBody(toJson(swarmClusterConfiguration));
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals("[]", response.responseBody(), true);
        }

        @Test
        void shouldValidateAConfigurationWithAllPrivateRegistryInfos() throws Exception {
            SwarmClusterConfiguration swarmClusterConfiguration = new SwarmClusterConfiguration()
                    .setMaxDockerContainers("1")
                    .setDockerURI("https://api.example.com")
                    .setDockerClientCert("some ca cert")
                    .setDockerClientKey("some client key")
                    .setDockerClientCert("some client cert")
                    .setGoServerUrl("https://ci.example.com/go")
                    .setAutoRegisterTimeout("10")
                    .setUseDockerAuthInfo("true")
                    .setPrivateRegistryServer("server")
                    .setPrivateRegistryUsername("username")
                    .setPrivateRegistryPassword("password");

            request.setRequestBody(toJson(swarmClusterConfiguration));
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals("[]", response.responseBody(), true);
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/cluster-profile-invalid-docker-registry-settings-error.json")
        void shouldNotValidateAConfigurationWithInvalidPrivateRegistrySettings(String expectedJSON) throws Exception {
            SwarmClusterConfiguration swarmClusterConfiguration = new SwarmClusterConfiguration()
                    .setMaxDockerContainers("1")
                    .setDockerURI("https://api.example.com")
                    .setDockerClientCert("some ca cert")
                    .setDockerClientKey("some client key")
                    .setDockerClientCert("some client cert")
                    .setGoServerUrl("https://ci.example.com/go")
                    .setAutoRegisterTimeout("10")
                    .setUseDockerAuthInfo("true")
                    .setPrivateRegistryServer("")
                    .setPrivateRegistryUsername("")
                    .setPrivateRegistryPassword("");

            request.setRequestBody(toJson(swarmClusterConfiguration));
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
        }
    }

    @Nested
    class ElasticProfileView {
        @Test
        void shouldRenderTheTemplateInJSON() throws Exception {
            GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-elastic-agent-profile-view"));

            assertThat(response.responseCode()).isEqualTo(200);

            Map<String, String> hashSet = fromJSON(response.responseBody());
            assertThat(hashSet)
                    .containsEntry("template", readResource("/docker-swarm/elastic-profile.template.html"));
        }

        @Test
        void allFieldsShouldBePresentInView() {
            String template = readResource("/docker-swarm/elastic-profile.template.html");
            Document document = Jsoup.parse(template);

            for (MetadataHolder field : new MetadataExtractor().forClass(SwarmElasticProfileConfiguration.class)) {
                assertThat(getInputOrTextArea(document, field.getKey()))
                        .describedAs(format("Field %s must be present", field.getKey()))
                        .isNotNull();
                assertThat(document.selectFirst(format("span[ng-show=\"GOINPUTNAME[%s].$error.server\"]", field.getKey())))
                        .isNotNull()
                        .satisfies(span -> assertThat(span.text())
                                .isEqualTo(format("{{GOINPUTNAME[%s].$error.server}}", field.getKey()))
                        );
            }
        }
    }

    @Nested
    class ElasticProfileMetadata {
        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/elastic-profile-metadata.json")
        void shouldSerializeAllFields(String metadataJson) throws Exception {
            GoPluginApiResponse response = plugin.handle(request("cd.go.elastic-agent.get-elastic-agent-profile-metadata"));

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(metadataJson, response.responseBody(), true);
        }
    }

    @Nested
    class ElasticProfileValidation {
        private DefaultGoPluginApiRequest request;

        @BeforeEach
        void setUp() {
            request = request("cd.go.elastic-agent.validate-elastic-agent-profile");
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/elastic-profile-unknown-fields-error.json")
        void shouldBarfWhenUnknownKeysArePassed(String expectedJson) throws Exception {
            request.setRequestBody(toJson(Map.of(
                    "Image", "alpine:latest",
                    "foo", "value"
            )));
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(expectedJson, response.responseBody(), true);
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = "/docker-swarm/elastic-profile-mandatory-fields-error.json")
        void shouldValidateMandatoryKeys(String expectedJSON) throws Exception {
            GoPluginApiResponse response = plugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
        }
    }

    private Element getInputOrTextArea(Document document, String fieldName) {
        return document.selectFirst(format("input[ng-model=\"%s\"], textarea[ng-model=\"%s\"]", fieldName, fieldName));
    }

    private DefaultGoPluginApiRequest request(String requestName) {
        String extension = PLUGIN_IDENTIFIER.getExtension();
        String version = PLUGIN_IDENTIFIER.getSupportedExtensionVersions().get(0);
        return new DefaultGoPluginApiRequest(extension, version, requestName);
    }

    private static Map<String, String> fromJSON(String json) {
        return fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }
}
