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

package cd.go.contrib.elasticagents.common.requests;

import cd.go.contrib.elasticagents.common.models.ClusterProfileConfiguration;
import cd.go.contrib.elasticagents.common.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Getter
@Setter
@Accessors(chain = true)
public abstract class AbstractCreateAgentRequest<E extends ElasticProfileConfiguration, C extends ClusterProfileConfiguration> {
    @Expose
    @SerializedName("auto_register_key")
    protected String autoRegisterKey;

    @Expose
    @SerializedName("elastic_agent_profile_properties")
    protected E elasticProfileConfiguration;

    @Expose
    @SerializedName("environment")
    protected String environment;

    @Expose
    @SerializedName("job_identifier")
    protected JobIdentifier jobIdentifier;

    @Expose
    @SerializedName("cluster_profile_properties")
    protected C clusterProfileProperties;

    public AbstractCreateAgentRequest() {
    }

    public Collection<String> autoregisterPropertiesAsEnvironmentVars(String elasticAgentId, String pluginId) {
        List<String> vars = new ArrayList<>();
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_AGENT_ID=" + elasticAgentId);
        vars.add("GO_EA_AUTO_REGISTER_ELASTIC_PLUGIN_ID=" + pluginId);
        if (isNotBlank(autoRegisterKey)) {
            vars.add("GO_EA_AUTO_REGISTER_KEY=" + autoRegisterKey);
        }
        if (isNotBlank(environment)) {
            vars.add("GO_EA_AUTO_REGISTER_ENVIRONMENT=" + environment);
        }
        return vars;
    }
}
