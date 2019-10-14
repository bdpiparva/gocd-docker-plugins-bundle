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
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.Constants;
import cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPluginSettings;
import cd.go.contrib.elasticagents.dockerswarm.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.requests.MigrateConfigurationRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.util.List.of;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class MigrateConfigurationRequestExecutorTest {
    private ClusterProfileProperties clusterProfileProperties;
    private ClusterProfile<ClusterProfileProperties> clusterProfile;
    private ElasticAgentProfile<ElasticProfileConfiguration> elasticAgentProfile;
    private ElasticProfileConfiguration elasticProfileConfiguration;

    @Before
    public void setUp() {
        clusterProfileProperties = new ClusterProfileProperties();
        clusterProfileProperties.setGoServerUrl("https://127.0.0.1:8154/go");
        clusterProfileProperties.setAutoRegisterTimeout("20");

        clusterProfile = new ClusterProfile<>();
        clusterProfile.setId("cluster_profile_id");
        clusterProfile.setPluginId(Constants.PLUGIN_ID);
        clusterProfile.setClusterProfileProperties(clusterProfileProperties);

        elasticAgentProfile = new ElasticAgentProfile<>();
        elasticAgentProfile.setId("profile_id");
        elasticAgentProfile.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile.setClusterProfileId("cluster_profile_id");
        elasticProfileConfiguration = new ElasticProfileConfiguration()
                .setImage("Foo");
        elasticAgentProfile.setElasticProfileConfiguration(elasticProfileConfiguration);
    }

    @Test
    public void shouldNotMigrateConfigWhenNoPluginSettingsAreConfigured() {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(new DockerSwarmPluginSettings())
                .setClusterProfiles(of(clusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(new ClusterProfileProperties()));
        assertThat(responseObject.getClusterProfiles(), is(of(clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(of(elasticAgentProfile)));
    }

    @Test
    public void shouldNotMigrateConfigWhenClusterProfileIsAlreadyConfigured() {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest();
        request.setPluginSettings(new DockerSwarmPluginSettings())
                .setClusterProfiles(of(clusterProfile))
                .setElasticAgentProfiles(of(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute(request);

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));
        assertThat(responseObject.getClusterProfiles(), is(of(clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(of(elasticAgentProfile)));
    }

    @Test
    public void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations() throws Exception {
        ClusterProfile emptyClusterProfile = new ClusterProfile(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID), Constants.PLUGIN_ID, new ClusterProfileProperties());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, of(emptyClusterProfile), of(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId(), is(not(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID))));
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual, is(of(this.clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(of(elasticAgentProfile)));

        assertThat(elasticAgentProfile.getClusterProfileId(), is(actualClusterProfile.getId()));
    }

    @Test
    public void shouldPopulateNoOpClusterProfileWithPluginSettingsConfigurations_WithoutChangingClusterProfileIdIfItsNotNoOp() throws Exception {
        String clusterProfileId = "i-renamed-no-op-cluster-to-something-else";
        ClusterProfile emptyClusterProfile = new ClusterProfile(clusterProfileId, Constants.PLUGIN_ID, new ClusterProfileProperties());
        elasticAgentProfile.setClusterProfileId(emptyClusterProfile.getId());
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, of(emptyClusterProfile), of(elasticAgentProfile));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);

        assertThat(actualClusterProfile.getId(), is(clusterProfileId));
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual, is(of(this.clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(of(elasticAgentProfile)));

        assertThat(elasticAgentProfile.getClusterProfileId(), is(clusterProfileId));
    }

    @Test
    public void shouldMigratePluginSettingsToClusterProfile_WhenNoElasticAgentProfilesAreConfigured() throws Exception {
        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, Collections.emptyList(), Collections.emptyList());
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));
        List<ClusterProfile> actual = responseObject.getClusterProfiles();
        ClusterProfile actualClusterProfile = actual.get(0);
        this.clusterProfile.setId(actualClusterProfile.getId());

        assertThat(actual, is(of(this.clusterProfile)));
        assertThat(responseObject.getElasticAgentProfiles(), is(Collections.emptyList()));
    }

    @Test
    public void ShouldMigrateEmptyClusterProfiles_WhenMultipleEmptyClusterProfilesExists() throws Exception {
        ClusterProfile emptyCluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, new ClusterProfileProperties());
        ClusterProfile emptyCluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, new ClusterProfileProperties());

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, of(emptyCluster1, emptyCluster2), of(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0), is(clusterProfile));

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(1).getId());
        assertThat(responseObject.getClusterProfiles().get(1), is(clusterProfile));

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId(), is(emptyCluster1.getId()));
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId(), is(emptyCluster2.getId()));
    }

    @Test
    public void ShouldNotMigrateEmptyAndUnassociatedClusterProfiles() throws Exception {
        ClusterProfile emptyCluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, new ClusterProfileProperties());
        ClusterProfile emptyCluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, new ClusterProfileProperties());

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(emptyCluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(emptyCluster1.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, of(emptyCluster1, emptyCluster2), of(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));

        this.clusterProfile.setId(responseObject.getClusterProfiles().get(0).getId());
        assertThat(responseObject.getClusterProfiles().get(0), is(clusterProfile));

        //verify cluster is empty.. not migrated
        assertThat(responseObject.getClusterProfiles().get(1), is(emptyCluster2));

        assertThat(responseObject.getElasticAgentProfiles().get(0).getClusterProfileId(), is(emptyCluster1.getId()));
        assertThat(responseObject.getElasticAgentProfiles().get(1).getClusterProfileId(), is(emptyCluster1.getId()));
    }

    @Test
    public void shouldNotMigrateConfigWhenMultipleClusterProfilesAreAlreadyMigrated() throws Exception {
        ClusterProfile cluster1 = new ClusterProfile("cluster_profile_1", Constants.PLUGIN_ID, clusterProfileProperties);
        ClusterProfile cluster2 = new ClusterProfile("cluster_profile_2", Constants.PLUGIN_ID, clusterProfileProperties);

        ElasticAgentProfile elasticAgentProfile1 = new ElasticAgentProfile();
        elasticAgentProfile1.setId("profile_id_1");
        elasticAgentProfile1.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile1.setClusterProfileId(cluster1.getId());

        ElasticAgentProfile elasticAgentProfile2 = new ElasticAgentProfile();
        elasticAgentProfile2.setId("profile_id_2");
        elasticAgentProfile2.setPluginId(Constants.PLUGIN_ID);
        elasticAgentProfile2.setClusterProfileId(cluster2.getId());

        MigrateConfigurationRequest request = new MigrateConfigurationRequest(clusterProfileProperties, of(cluster1, cluster2), of(elasticAgentProfile1, elasticAgentProfile2));
        MigrateConfigurationRequestExecutor executor = new MigrateConfigurationRequestExecutor();

        GoPluginApiResponse response = executor.execute();

        MigrateConfigurationRequest responseObject = fromJson(response.responseBody(), null);

        assertThat(responseObject.getPluginSettings(), is(clusterProfileProperties));

        assertThat(responseObject.getClusterProfiles(), is(of(cluster1, cluster2)));

        assertThat(responseObject.getElasticAgentProfiles(), is(of(elasticAgentProfile1, elasticAgentProfile2)));
    }

}
