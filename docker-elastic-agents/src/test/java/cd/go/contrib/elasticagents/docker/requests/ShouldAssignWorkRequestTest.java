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

import cd.go.contrib.elasticagents.docker.Agent;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.utils.JobIdentifierMother;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ShouldAssignWorkRequestTest {

    @Test
    public void shouldDeserializeFromJSON() {
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

        ShouldAssignWorkRequest request = ShouldAssignWorkRequest.fromJSON(json.toString());

        assertThat(request.getEnvironment(), equalTo("prod"));
        assertThat(request.getAgent(), equalTo(new Agent("42", Agent.AgentState.Idle, Agent.BuildState.Idle, Agent.ConfigState.Enabled)));
        assertThat(request.getJobIdentifier(), is(JobIdentifierMother.get()));
        assertThat(request.getElasticProfileConfiguration(), is(new ElasticProfileConfiguration().setImage("alpine:latest")));
        assertThat(request.getClusterProfileProperties(), is(new ClusterProfileProperties().setGoServerUrl("some-url")));
    }
}
