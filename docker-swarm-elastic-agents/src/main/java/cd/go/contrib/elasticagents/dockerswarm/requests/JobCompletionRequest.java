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

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.*;
import cd.go.contrib.elasticagents.dockerswarm.executors.JobCompletionRequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class JobCompletionRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public JobCompletionRequest() {
    }

    public JobCompletionRequest(String elasticAgentId,
                                JobIdentifier jobIdentifier,
                                Map<String, String> clusterProfile) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfile);
    }

    public JobCompletionRequest(String elasticAgentId,
                                JobIdentifier jobIdentifier,
                                ClusterProfileProperties profileProperties) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = profileProperties;
    }

    public static JobCompletionRequest fromJSON(String json) {
        JobCompletionRequest jobCompletionRequest = Util.GSON.fromJson(json, JobCompletionRequest.class);
        return jobCompletionRequest;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public RequestExecutor executor(AgentInstances<DockerService> agentInstances, ElasticAgentRequestClient pluginRequest) {
        return new JobCompletionRequestExecutor(this, agentInstances, pluginRequest);
    }

    @Override
    public String toString() {
        return "JobCompletionRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }

    public ClusterProfileProperties getClusterProfileProperties() {
        return clusterProfileProperties;
    }
}
