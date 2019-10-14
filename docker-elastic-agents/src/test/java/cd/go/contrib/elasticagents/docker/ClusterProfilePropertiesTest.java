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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.docker.requests.JobCompletionRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterProfilePropertiesTest {

    @Test
    void shouldGenerateSameUUIDForClusterProfileProperties() {
        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties().setGoServerUrl("http://go-server-url/go");

        assertThat(clusterProfileProperties.uuid()).isEqualTo(clusterProfileProperties.uuid());
    }

    @Test
    void shouldGenerateSameUUIDForClusterProfilePropertiesAcrossRequests() {
        String createAgentRequestJSON = "{\n" +
                "  \"auto_register_key\": \"secret-key\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"key1\": \"value1\",\n" +
                "    \"key2\": \"value2\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"https://foo.com/go\",\n" +
                "    \"docker_uri\": \"unix:///var/run/docker.sock\"\n" +
                "  },\n" +
                "  \"environment\": \"prod\"\n" +
                "}";

        CreateAgentRequest createAgentRequest = CreateAgentRequest.fromJSON(createAgentRequestJSON, CreateAgentRequest.class);

        String jobCompletionRequestJSON = "{\n" +
                "  \"elastic_agent_id\": \"ea1\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"Image\": \"alpine:latest\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"https://foo.com/go\", \n" +
                "    \"docker_uri\": \"unix:///var/run/docker.sock\"\n" +
                "  },\n" +
                "  \"job_identifier\": {\n" +
                "    \"pipeline_name\": \"test-pipeline\",\n" +
                "    \"pipeline_counter\": 1,\n" +
                "    \"pipeline_label\": \"Test Pipeline\",\n" +
                "    \"stage_name\": \"test-stage\",\n" +
                "    \"stage_counter\": \"1\",\n" +
                "    \"job_name\": \"test-job\",\n" +
                "    \"job_id\": 100\n" +
                "  }\n" +
                "}";

        JobCompletionRequest jobCompletionRequest = JobCompletionRequest.fromJSON(jobCompletionRequestJSON, JobCompletionRequest.class);
        assertThat(jobCompletionRequest.getClusterProfileConfiguration().uuid()).isEqualTo(createAgentRequest.getClusterProfileProperties().uuid());

    }
}
