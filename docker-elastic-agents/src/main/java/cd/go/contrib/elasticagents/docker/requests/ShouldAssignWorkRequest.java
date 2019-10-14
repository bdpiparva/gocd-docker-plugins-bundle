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

import cd.go.contrib.elasticagents.common.Agent;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.common.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import static cd.go.plugin.base.GsonTransformer.fromJson;

/**
 * Represents the {@link cd.go.contrib.elasticagents.docker.Request#REQUEST_SHOULD_ASSIGN_WORK} message.
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
public class ShouldAssignWorkRequest {
    @Expose
    @SerializedName("agent")
    private Agent agent;
    @Expose
    @SerializedName("environment")
    private String environment;
    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;
    @Expose
    @SerializedName("elastic_agent_profile_properties")
    private ElasticProfileConfiguration elasticProfileConfiguration;
    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public ShouldAssignWorkRequest() {
    }


    public static ShouldAssignWorkRequest fromJSON(String json) {
        return fromJson(json, ShouldAssignWorkRequest.class);
    }
}
