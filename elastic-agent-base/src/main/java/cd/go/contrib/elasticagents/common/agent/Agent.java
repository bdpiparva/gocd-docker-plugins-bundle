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

package cd.go.contrib.elasticagents.common.agent;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static cd.go.plugin.base.GsonTransformer.toJson;

@ToString
@EqualsAndHashCode
public class Agent {
    private static final Type AGENT_METADATA_LIST_TYPE = new TypeToken<ArrayList<Agent>>() {
    }.getType();

    @Expose
    @SerializedName("agent_id")
    private String agentId;

    @Expose
    @SerializedName("agent_state")
    private AgentState agentState;

    @Expose
    @SerializedName("build_state")
    private AgentBuildState agentBuildState;

    @Expose
    @SerializedName("config_state")
    private AgentConfigState agentConfigState;

    // Public constructor needed for JSON de-serialization
    public Agent() {
    }

    public Agent(String agentId) {
        this(agentId, null, null, null);
    }

    // Used in tests
    public Agent(String agentId,
                 AgentState agentState,
                 AgentBuildState agentBuildState,
                 AgentConfigState agentConfigState) {
        this.agentId = agentId;
        this.agentState = agentState;
        this.agentBuildState = agentBuildState;
        this.agentConfigState = agentConfigState;
    }

    public String elasticAgentId() {
        return agentId;
    }

    public void elasticAgentId(String agentId) {
        this.agentId = agentId;
    }

    public AgentState agentState() {
        return agentState;
    }

    public AgentBuildState buildState() {
        return agentBuildState;
    }

    public AgentConfigState configState() {
        return agentConfigState;
    }

    public static List<Agent> fromJSONArray(String json) {
        return fromJson(json, AGENT_METADATA_LIST_TYPE);
    }

    public static String toJSONArray(Collection<Agent> metadata) {
        return toJson(metadata);
    }
}
