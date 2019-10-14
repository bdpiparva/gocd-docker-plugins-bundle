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
import cd.go.contrib.elasticagents.dockerswarm.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.executors.AgentStatusReportExecutor;
import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class AgentStatusReportRequest {
    @Expose
    @SerializedName("elastic_agent_id")
    private String elasticAgentId;

    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public AgentStatusReportRequest() {
    }

    public AgentStatusReportRequest(String elasticAgentId,
                                    JobIdentifier jobIdentifier,
                                    Map<String, String> clusterProfileProperties) {
        this.elasticAgentId = elasticAgentId;
        this.jobIdentifier = jobIdentifier;
        this.clusterProfileProperties = ClusterProfileProperties.fromConfiguration(clusterProfileProperties);
    }

    public static AgentStatusReportRequest fromJSON(String json) {
        return Util.GSON.fromJson(json, AgentStatusReportRequest.class);
    }

    public ClusterProfileProperties getClusterProfileProperties() {
        return clusterProfileProperties;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public AgentStatusReportExecutor executor(ElasticAgentRequestClient pluginRequest,
                                              AgentInstances<DockerService> agentInstances) throws IOException {
        return new AgentStatusReportExecutor(this);
    }

    @Override
    public String toString() {
        return "AgentStatusReportRequest{" +
                "elasticAgentId='" + elasticAgentId + '\'' +
                ", jobIdentifier=" + jobIdentifier +
                ", clusterProfileProperties=" + clusterProfileProperties +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentStatusReportRequest that = (AgentStatusReportRequest) o;
        return Objects.equals(elasticAgentId, that.elasticAgentId) &&
                Objects.equals(jobIdentifier, that.jobIdentifier) &&
                Objects.equals(clusterProfileProperties, that.clusterProfileProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elasticAgentId, jobIdentifier, clusterProfileProperties);
    }
}
