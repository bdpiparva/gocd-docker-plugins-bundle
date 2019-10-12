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
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CreateAgentRequestTest {

    @Test
    public void shouldDeserializeFromJSON() {
        String json = "{\n" +
                "  \"auto_register_key\": \"secret-key\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"Image\": \"alpine\"\n" +
                "  },\n" +
                "  \"cluster_profile_properties\": {\n" +
                "    \"go_server_url\": \"https://foo.com/go\",\n" +
                "    \"docker_uri\": \"unix:///var/run/docker.sock\"\n" +
                "  },\n" +
                "  \"environment\": \"prod\"\n" +
                "}";

        CreateAgentRequest request = CreateAgentRequest.fromJSON(json);
        assertThat(request.getAutoRegisterKey(), equalTo("secret-key"));
        assertThat(request.getEnvironment(), equalTo("prod"));
        assertThat(request.getElasticProfileConfiguration().getImage(), is("alpine"));

        ClusterProfileProperties expectedClusterProfileProperties = new ClusterProfileProperties();
        expectedClusterProfileProperties.setGoServerUrl("https://foo.com/go");
        expectedClusterProfileProperties.setDockerURI("unix:///var/run/docker.sock");
        assertThat(request.getClusterProfileProperties(), is(expectedClusterProfileProperties));
    }
}
