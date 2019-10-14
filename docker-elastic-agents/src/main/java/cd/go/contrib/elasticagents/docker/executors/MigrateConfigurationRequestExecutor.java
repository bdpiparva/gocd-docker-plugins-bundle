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

import cd.go.contrib.elasticagents.common.models.ClusterProfile;
import cd.go.contrib.elasticagents.common.models.ElasticAgentProfile;
import cd.go.contrib.elasticagents.docker.Constants;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.requests.MigrateConfigurationRequest;
import cd.go.plugin.base.executors.AbstractExecutor;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static cd.go.contrib.elasticagents.docker.DockerPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.util.List.of;

public class MigrateConfigurationRequestExecutor extends AbstractExecutor<MigrateConfigurationRequest> {
    @Override
    protected MigrateConfigurationRequest parseRequest(String requestBody) {
        return fromJson(requestBody, MigrateConfigurationRequest.class);
    }

    @Override
    protected GoPluginApiResponse execute(MigrateConfigurationRequest request) {
        LOG.info("[Migrate Config] Request for Config Migration Started...");

        ClusterProfileProperties clusterProfileProperties = request.getPluginSettings();
        List<ClusterProfile<ClusterProfileProperties>> existingClusterProfiles = request.getClusterProfiles();
        List<ElasticAgentProfile<ElasticProfileConfiguration>> existingElasticAgentProfiles = request.getElasticAgentProfiles();

        if (!arePluginSettingsConfigured(clusterProfileProperties)) {
            LOG.info("[Migrate Config] No Plugin Settings are configured. Skipping Config Migration...");
            return new DefaultGoPluginApiResponse(200, request.toJSON());
        }

        if (existingClusterProfiles.size() == 0) {
            LOG.info("[Migrate Config] Did not find any Cluster Profile. Possibly, user just have configured plugin settings and haven't define any elastic agent profiles.");
            String newClusterId = UUID.randomUUID().toString();
            LOG.info("[Migrate Config] Migrating existing plugin settings to new cluster profile '{}'", newClusterId);
            ClusterProfile<ClusterProfileProperties> clusterProfile = new ClusterProfile(newClusterId, Constants.PLUGIN_ID, clusterProfileProperties);

            return getGoPluginApiResponse(clusterProfileProperties, of(clusterProfile), existingElasticAgentProfiles);
        }

        LOG.info("[Migrate Config] Checking to perform migrations on Cluster Profiles '{}'.", existingClusterProfiles.stream().map(ClusterProfile::getId).collect(Collectors.toList()));

        for (ClusterProfile<ClusterProfileProperties> clusterProfile : existingClusterProfiles) {
            List<ElasticAgentProfile<ElasticProfileConfiguration>> associatedElasticAgentProfiles = findAssociatedElasticAgentProfiles(clusterProfile, existingElasticAgentProfiles);
            if (associatedElasticAgentProfiles.size() == 0) {
                LOG.info("[Migrate Config] Skipping migration for the cluster '{}' as no Elastic Agent Profiles are associated with it.", clusterProfile.getId());
                continue;
            }

            if (!arePluginSettingsConfigured(clusterProfile.getClusterProfileProperties())) {
                List<String> associatedProfileIds = associatedElasticAgentProfiles.stream().map(ElasticAgentProfile::getId).collect(Collectors.toList());
                LOG.info("[Migrate Config] Found an empty cluster profile '{}' associated with '{}' elastic agent profiles.", clusterProfile.getId(), associatedProfileIds);
                migrateConfigForCluster(clusterProfileProperties, associatedElasticAgentProfiles, clusterProfile);
            } else {
                LOG.info("[Migrate Config] Skipping migration for the cluster '{}' as cluster has already been configured.", clusterProfile.getId());
            }
        }

        return new DefaultGoPluginApiResponse(200, request.toJSON());
    }

    //this is responsible to copy over plugin settings configurations to cluster profile and if required rename no op cluster
    private void migrateConfigForCluster(ClusterProfileProperties clusterProfileProperties,
                                         List<ElasticAgentProfile<ElasticProfileConfiguration>> associatedElasticAgentProfiles,
                                         ClusterProfile clusterProfile) {
        LOG.info("[Migrate Config] Coping over existing plugin settings configurations to '{}' cluster profile.", clusterProfile.getId());
        clusterProfile.setClusterProfileProperties(clusterProfileProperties);

        if (clusterProfile.getId().equals(String.format("no-op-cluster-for-%s", Constants.PLUGIN_ID))) {
            String newClusterId = UUID.randomUUID().toString();
            LOG.info("[Migrate Config] Renaming dummy cluster profile from '{}' to '{}'.", clusterProfile.getId(), newClusterId);
            clusterProfile.setId(newClusterId);

            LOG.info("[Migrate Config] Changing all elastic agent profiles to point to '{}' cluster profile.", clusterProfile.getId());
            associatedElasticAgentProfiles.forEach(elasticAgentProfile -> elasticAgentProfile.setClusterProfileId(newClusterId));
        }
    }

    private List<ElasticAgentProfile<ElasticProfileConfiguration>> findAssociatedElasticAgentProfiles(ClusterProfile<ClusterProfileProperties> clusterProfile,
                                                                                                      List<ElasticAgentProfile<ElasticProfileConfiguration>> elasticAgentProfiles) {
        return elasticAgentProfiles.stream().filter(profile -> Objects.equals(profile.getClusterProfileId(), clusterProfile.getId())).collect(Collectors.toList());
    }

    private GoPluginApiResponse getGoPluginApiResponse(ClusterProfileProperties clusterProfileProperties,
                                                       List<ClusterProfile<ClusterProfileProperties>> clusterProfiles,
                                                       List<ElasticAgentProfile<ElasticProfileConfiguration>> elasticAgentProfiles) {
        MigrateConfigurationRequest response = new MigrateConfigurationRequest();
        response.setPluginSettings(clusterProfileProperties);
        response.setClusterProfiles(clusterProfiles);
        response.setElasticAgentProfiles(elasticAgentProfiles);

        return new DefaultGoPluginApiResponse(200, response.toJSON());
    }

    private boolean arePluginSettingsConfigured(ClusterProfileProperties clusterProfileProperties) {
        return !StringUtils.isBlank(clusterProfileProperties.getGoServerUrl());
    }
}
