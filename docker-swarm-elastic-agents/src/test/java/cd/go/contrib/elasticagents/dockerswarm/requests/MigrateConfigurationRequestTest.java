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

package cd.go.contrib.elasticagents.dockerswarm.requests;

import cd.go.contrib.elasticagents.common.models.ClusterProfile;
import cd.go.contrib.elasticagents.common.models.ElasticAgentProfile;
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.ElasticProfileConfiguration;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class MigrateConfigurationRequestTest {
    @Test
    void shouldCreateMigrationConfigRequestFromRequestBody() {
        String requestBody = "{" +
                "    \"plugin_settings\":{" +
                "        \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "        \"auto_register_timeout\":\"20\"" +
                "    }," +
                "    \"cluster_profiles\":[" +
                "        {" +
                "            \"id\":\"cluster_profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"properties\":{" +
                "                \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "                \"auto_register_timeout\":\"20\"" +
                "            }" +
                "         }" +
                "    ]," +
                "    \"elastic_agent_profiles\":[" +
                "        {" +
                "            \"id\":\"profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"cluster_profile_id\":\"cluster_profile_id\"," +
                "            \"properties\":{" +
                "                \"some_key\":\"some_value\"," +
                "                \"some_key2\":\"some_value2\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        MigrateConfigurationRequest request = fromJson(requestBody, MigrateConfigurationRequest.class);

        DockerSwarmPluginSettings pluginSettings = new DockerSwarmPluginSettings();
        pluginSettings.setGoServerUrl("https://127.0.0.1:8154/go");
        pluginSettings.setAutoRegisterTimeout("20");

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setGoServerUrl("https://127.0.0.1:8154/go");
        clusterProfileProperties.setAutoRegisterTimeout("20");

        ClusterProfile<ClusterProfileProperties> clusterProfile = new ClusterProfile<>();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(clusterProfileProperties);

        ElasticAgentProfile<ElasticProfileConfiguration> elasticAgentProfile = new ElasticAgentProfile<>();
        elasticAgentProfile.setId("profile_id")
                .setPluginId("plugin_id")
                .setClusterProfileId("cluster_profile_id")
                .setElasticProfileConfiguration(new ElasticProfileConfiguration());

        assertThat(pluginSettings).isEqualTo(request.getPluginSettings());
        assertThat(of(clusterProfile)).isEqualTo(request.getClusterProfiles());
        assertThat(of(elasticAgentProfile)).isEqualTo(request.getElasticAgentProfiles());
    }

    @Test
    void shouldCreateMigrationConfigRequestWhenNoConfigurationsAreSpecified() {
        String requestBody = "{" +
                "    \"plugin_settings\":{}," +
                "    \"cluster_profiles\":[]," +
                "    \"elastic_agent_profiles\":[]" +
                "}\n";

        MigrateConfigurationRequest request = fromJson(requestBody, MigrateConfigurationRequest.class);

        assertThat(new DockerSwarmPluginSettings()).isEqualTo(request.getPluginSettings());
        assertThat(of()).isEqualTo(request.getClusterProfiles());
        assertThat(of()).isEqualTo(request.getElasticAgentProfiles());
    }

    @Test
    void shouldSerializeToJSONFromMigrationConfigRequest() throws JSONException {
        DockerSwarmPluginSettings pluginSettings = new DockerSwarmPluginSettings();
        pluginSettings.setGoServerUrl("https://127.0.0.1:8154/go");
        pluginSettings.setAutoRegisterTimeout("20");

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setGoServerUrl("https://127.0.0.1:8154/go");
        clusterProfileProperties.setAutoRegisterTimeout("10");

        ClusterProfile<ClusterProfileProperties> clusterProfile = new ClusterProfile<>();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(clusterProfileProperties);

        ElasticAgentProfile<ElasticProfileConfiguration> elasticAgentProfile = new ElasticAgentProfile<>();
        elasticAgentProfile.setId("profile_id")
                .setPluginId("plugin_id")
                .setClusterProfileId("cluster_profile_id")
                .setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine:latest"));

        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(clusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));

        String actual = request.toJSON();

        String expected = "{" +
                "    \"plugin_settings\":{" +
                "        \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "        \"auto_register_timeout\":\"20\"" +
                "    }," +
                "    \"cluster_profiles\":[" +
                "        {" +
                "            \"id\":\"cluster_profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"properties\":{" +
                "                \"go_server_url\":\"https://127.0.0.1:8154/go\", " +
                "                \"auto_register_timeout\":\"10\"" +
                "            }" +
                "         }" +
                "    ]," +
                "    \"elastic_agent_profiles\":[" +
                "        {" +
                "            \"id\":\"profile_id\"," +
                "            \"plugin_id\":\"plugin_id\"," +
                "            \"cluster_profile_id\":\"cluster_profile_id\"," +
                "            \"properties\":{" +
                "                \"Image\":\"alpine:latest\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        JSONAssert.assertEquals(expected, actual, true);
    }
}
