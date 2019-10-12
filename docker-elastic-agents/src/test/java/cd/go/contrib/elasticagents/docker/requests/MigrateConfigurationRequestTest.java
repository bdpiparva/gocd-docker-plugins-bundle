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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.models.ClusterProfile;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticAgentProfile;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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
                "                \"Image\":\"alpine:latest\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        MigrateConfigurationRequest request = MigrateConfigurationRequest.fromJSON(requestBody);

        ClusterProfileProperties pluginSettings = new ClusterProfileProperties();
        pluginSettings.setGoServerUrl("https://127.0.0.1:8154/go");
        pluginSettings.setAutoRegisterTimeout("20");

        ClusterProfile clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(pluginSettings);

        ElasticAgentProfile elasticAgentProfile = new ElasticAgentProfile()
                .setId("profile_id")
                .setPluginId("plugin_id")
                .setClusterProfileId("cluster_profile_id")
                .setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine:latest"));

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

        MigrateConfigurationRequest request = MigrateConfigurationRequest.fromJSON(requestBody);

        assertThat(new ClusterProfileProperties()).isEqualTo(request.getPluginSettings());
        assertThat(of()).isEqualTo(request.getClusterProfiles());
        assertThat(of()).isEqualTo(request.getElasticAgentProfiles());
    }

    @Test
    void shouldSerializeToJSONFromMigrationConfigRequest() throws JSONException {
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setGoServerUrl("https://127.0.0.1:8154/go");
        clusterProfileProperties.setAutoRegisterTimeout("20");

        ClusterProfile clusterProfile = new ClusterProfile();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId("plugin_id");
        clusterProfile.setClusterProfileProperties(clusterProfileProperties);

        ElasticAgentProfile elasticAgentProfile = new ElasticAgentProfile();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId("plugin_id");
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        elasticAgentProfile.setElasticProfileConfiguration(new ElasticProfileConfiguration().setImage("alpine:latest"));

        MigrateConfigurationRequest request = new MigrateConfigurationRequest()
                .setPluginSettings(clusterProfileProperties)
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
                "                \"Image\":\"alpine:latest\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}\n";

        JSONAssert.assertEquals(expected, actual, false);
    }
}
