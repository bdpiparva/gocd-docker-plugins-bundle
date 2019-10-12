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

import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.utils.JobIdentifierMother;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentStatusReportRequestTest {
    @Test
    void shouldDeserializeFromJSON() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());


        AgentStatusReportRequest expected = new AgentStatusReportRequest()
                .setElasticAgentId("some-id")
                .setJobIdentifier(JobIdentifierMother.get())
                .setClusterProfile(null);

        assertThat(agentStatusReportRequest).isEqualTo(expected);
    }

    @Test
    void shouldDeserializeFromJSONWithClusterProfile() {
        JsonObject jobIdentifierJson = JobIdentifierMother.getJson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("elastic_agent_id", "some-id");
        jsonObject.add("job_identifier", jobIdentifierJson);
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://foo.com/go");
        clusterJSON.addProperty("docker_uri", "unix:///var/run/docker.sock");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        AgentStatusReportRequest agentStatusReportRequest = AgentStatusReportRequest.fromJSON(jsonObject.toString());

        ClusterProfileProperties expectedClusterProfile = new ClusterProfileProperties()
                .setGoServerUrl("https://foo.com/go")
                .setDockerURI("unix:///var/run/docker.sock");

        AgentStatusReportRequest expected = new AgentStatusReportRequest()
                .setElasticAgentId("some-id")
                .setJobIdentifier(JobIdentifierMother.get())
                .setClusterProfile(expectedClusterProfile);
        assertThat(agentStatusReportRequest).isEqualTo(expected);
    }
}
