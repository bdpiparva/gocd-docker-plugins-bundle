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

import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.AgentBuildState;
import cd.go.contrib.elasticagents.common.agent.AgentConfigState;
import cd.go.contrib.elasticagents.common.agent.AgentState;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.utils.JobIdentifierMother;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.assertj.core.api.Assertions.assertThat;

class ShouldAssignWorkRequestTest {

    @Test
    void shouldDeserializeFromJSON() {
        JsonObject agentJson = new JsonObject();
        agentJson.addProperty("agent_id", "42");
        agentJson.addProperty("agent_state", "Idle");
        agentJson.addProperty("build_state", "Idle");
        agentJson.addProperty("config_state", "Enabled");

        JsonObject elasticProfileJson = new JsonObject();
        elasticProfileJson.addProperty("Image", "alpine:latest");

        JsonObject clusterProfileJson = new JsonObject();
        clusterProfileJson.addProperty("go_server_url", "some-url");

        JsonObject json = new JsonObject();
        json.addProperty("environment", "prod");
        json.add("agent", agentJson);
        json.add("job_identifier", JobIdentifierMother.getJson());
        json.add("elastic_agent_profile_properties", elasticProfileJson);
        json.add("cluster_profile_properties", clusterProfileJson);

        ShouldAssignWorkRequest request = fromJson(json.toString(), ShouldAssignWorkRequest.class);

        assertThat(request.getEnvironment()).isEqualTo("prod");
        assertThat(request.getAgent()).isEqualTo(new Agent("42", AgentState.Idle, AgentBuildState.Idle, AgentConfigState.Enabled));
        assertThat(request.getJobIdentifier()).isEqualTo(JobIdentifierMother.get());
        assertThat(request.getElasticProfileConfiguration()).isEqualTo(new ElasticProfileConfiguration().setImage("alpine:latest"));
        assertThat(request.getClusterProfileProperties()).isEqualTo(new ClusterProfileProperties().setGoServerUrl("some-url"));
    }
}
