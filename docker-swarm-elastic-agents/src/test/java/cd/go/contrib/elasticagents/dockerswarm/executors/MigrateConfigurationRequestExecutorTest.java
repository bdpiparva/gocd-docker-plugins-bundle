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

import cd.go.contrib.elasticagents.common.models.ClusterProfile;
import cd.go.contrib.elasticagents.common.models.ElasticAgentProfile;
import cd.go.contrib.elasticagents.dockerswarm.SwarmClusterConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.Constants;
import cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.SwarmElasticProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.requests.MigrateConfigurationRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class MigrateConfigurationRequestExecutorTest {
    private DockerSwarmPluginSettings pluginSettings;
    private ClusterProfile<SwarmClusterConfiguration> clusterProfile;
    private ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile;
    private MigrateConfigurationRequestExecutor executor;

    @BeforeEach
    void setUp() {
        pluginSettings = new DockerSwarmPluginSettings();
        pluginSettings.setGoServerUrl("https://127.0.0.1:8154/go");
        pluginSettings.setAutoRegisterTimeout("20");

        SwarmClusterConfiguration swarmClusterConfiguration = new SwarmClusterConfiguration()
                .setGoServerUrl("https://127.0.0.1:8154/go")
                .setAutoRegisterTimeout("20");

        clusterProfile = new ClusterProfile<>();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId(Constants.PLUGIN_ID);
        clusterProfile.setClusterProfileProperties(swarmClusterConfiguration);

        elasticAgentProfile = new ElasticAgentProfile<>();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        elasticAgentProfile.setElasticProfileConfiguration(
                new SwarmElasticProfileConfiguration().setImage("Foo")
        );
        executor = new MigrateConfigurationRequestExecutor();
    }

    @Test
    void shouldNotMigrateConfigWhenNoPluginSettingsAreConfigured() {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(new DockerSwarmPluginSettings())
                .setClusterProfiles(of(clusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(new DockerSwarmPluginSettings());
        assertThat(responseObject.getClusterProfiles()).isEqualTo(of(clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(of(elasticAgentProfile));
    }

    @Test
    void shouldNotMigrateConfigWhenClusterProfileIsAlreadyConfigured() {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(new DockerSwarmPluginSettings())
                .setClusterProfiles(of(clusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(new DockerSwarmPluginSettings());
        assertThat(responseObject.getClusterProfiles()).isEqualTo(of(clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(of(elasticAgentProfile));
    }

    @Test
    void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations() {
        ClusterProfile<SwarmClusterConfiguration> emptyClusterProfile = new ClusterProfile<>(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID), Constants.PLUGIN_ID, new SwarmClusterConfiguration());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(emptyClusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile<SwarmClusterConfiguration>> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId()).isNotEqualTo(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID));
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(of(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(of(elasticAgentProfile));

        assertThat(elasticAgentProfile.getClusterProfileId()).isEqualTo(actualClusterProfile.getId());
    }

    @Test
    void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations_WithoutChangingClusterProfileIdIfItsNotNoOp() {
        String clusterProfileId = "i-renamed-no-op-cluster-to-something-else";
        ClusterProfile<SwarmClusterConfiguration> emptyClusterProfile = new ClusterProfile<>(clusterProfileId, Constants.PLUGIN_ID, new SwarmClusterConfiguration());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(emptyClusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile<SwarmClusterConfiguration>> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId()).isEqualTo(clusterProfileId);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(of(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(of(elasticAgentProfile));

        assertThat(elasticAgentProfile.getClusterProfileId()).isEqualTo(clusterProfileId);
    }

    @Test
    void shouldMigratePluginSettingsToClusterProfile_WhenNoElasticAgentProfilesAreConfigured() {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(emptyList())
                .setElasticAgentProfiles(emptyList());

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);
        List<ClusterProfile<SwarmClusterConfiguration>> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual).isEqualTo(of(this.clusterProfile));
        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(emptyList());
    }

    @Test
    void ShouldMigrateEmptyClusterProfiles_WhenMultipleEmptyClusterProfilesExists() {
        ClusterProfile<SwarmClusterConfiguration> emptyCluster1 = new ClusterProfile<>("cluster_profile_1", Constants.PLUGIN_ID, new SwarmClusterConfiguration());
        ClusterProfile<SwarmClusterConfiguration> emptyCluster2 = new ClusterProfile<>("cluster_profile_2", Constants.PLUGIN_ID, new SwarmClusterConfiguration());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile1 = new ElasticAgentProfile<>();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile2 = new ElasticAgentProfile<>();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(emptyCluster1, emptyCluster2))
                .setElasticAgentProfiles(of(elasticAgentProfile1, elasticAgentProfile2));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0)).isEqualTo(clusterProfile);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(1).getId());
        assertThat(responseObject.getClusterProfiles().get(1)).isEqualTo(clusterProfile);

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId()).isEqualTo(emptyCluster2.getId());
    }

    @Test
    void ShouldNotMigrateEmptyAndUnassociatedClusterProfiles() {
        ClusterProfile<SwarmClusterConfiguration> emptyCluster1 = new ClusterProfile<>("cluster_profile_1", Constants.PLUGIN_ID, new SwarmClusterConfiguration());
        ClusterProfile<SwarmClusterConfiguration> emptyCluster2 = new ClusterProfile<>("cluster_profile_2", Constants.PLUGIN_ID, new SwarmClusterConfiguration());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile1 = new ElasticAgentProfile<>();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile2 = new ElasticAgentProfile<>();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster1.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(emptyCluster1, emptyCluster2))
                .setElasticAgentProfiles(of(elasticAgentProfile1, elasticAgentProfile2));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0)).isEqualTo(clusterProfile);

        //verify cluster is empty.. not migrated
        assertThat(responseObject.getClusterProfiles().get(1)).isEqualTo(emptyCluster2);

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId()).isEqualTo(emptyCluster1.getId());
    }

    @Test
    void shouldNotMigrateConfigWhenMultipleClusterProfilesAreAlreadyMigrated() {
        ClusterProfile<SwarmClusterConfiguration> cluster1 = new ClusterProfile<>("cluster_profile_1", Constants.PLUGIN_ID, new SwarmClusterConfiguration());
        ClusterProfile<SwarmClusterConfiguration> cluster2 = new ClusterProfile<>("cluster_profile_2", Constants.PLUGIN_ID, new SwarmClusterConfiguration());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile1 = new ElasticAgentProfile<>();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(cluster1.getId());

        ElasticAgentProfile<SwarmElasticProfileConfiguration> elasticAgentProfile2 = new ElasticAgentProfile<>();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(cluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(pluginSettings)
                .setClusterProfiles(of(cluster1, cluster2))
                .setElasticAgentProfiles(of(elasticAgentProfile1, elasticAgentProfile2));

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), MigrateConfigurationRequest.class);

        assertThat(responseObject.getPluginSettings()).isEqualTo(pluginSettings);

        assertThat(responseObject.getClusterProfiles()).isEqualTo(of(cluster1, cluster2));

        assertThat(responseObject.getElasticAgentProfiles()).isEqualTo(of(elasticAgentProfile1, elasticAgentProfile2));
    }
}
