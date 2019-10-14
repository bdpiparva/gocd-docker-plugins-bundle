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

import cd.go.contrib.elasticagents.docker.Constants;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.common.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Getter
@Setter
@Accessors(chain = true)
public class CreateAgentRequest {
    @Expose
    @SerializedName("auto_register_key")
    private String autoRegisterKey;

    @Expose
    @SerializedName("elastic_agent_profile_properties")
    private ElasticProfileConfiguration elasticProfileConfiguration;

    @Expose
    @SerializedName("environment")
    private String environment;

    @Expose
    @SerializedName("job_identifier")
    private JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfileProperties;

    public CreateAgentRequest() {
    }

    public static CreateAgentRequest fromJSON(String json) {
        return fromJson(json, CreateAgentRequest.class);
    }

    public Collection<String> autoregisterPropertiesAsEnvironmentVars(String elasticAgentId) {
        ArrayList<String> vars = new ArrayList<>();
        if (isNotBlank(autoRegisterKey)) {
            vars.add("GO_EA_AUTO_REGISTER_KEY=" + autoRegisterKey);
        }
        if (isNotBlank(environment)) {
            vars.add("GO_EA_AUTO_REGISTER_ENVIRONMENT=" + environment);
        }
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + elasticAgentId);
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + Constants.PLUGIN_ID);
        return vars;
    }
}
