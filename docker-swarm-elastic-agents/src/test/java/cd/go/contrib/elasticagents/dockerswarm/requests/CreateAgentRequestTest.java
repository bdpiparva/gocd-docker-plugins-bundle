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

import cd.go.contrib.elasticagents.dockerswarm.SwarmElasticProfileConfiguration;
import org.junit.jupiter.api.Test;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.assertj.core.api.Assertions.assertThat;

class CreateAgentRequestTest {

    @Test
    void shouldDeserializeFromJSON() {
        String json = "{\n" +
                "  \"auto_register_key\": \"secret-key\",\n" +
                "  \"elastic_agent_profile_properties\": {\n" +
                "    \"Image\": \"alpine:latest\",\n" +
                "    \"MaxMemory\": \"1G\"\n" +
                "  },\n" +
                "  \"environment\": \"prod\"\n" +
                "}";

        CreateAgentRequest request = fromJson(json, CreateAgentRequest.class);
        assertThat(request.getAutoRegisterKey()).isEqualTo("secret-key");
        assertThat(request.getEnvironment()).isEqualTo("prod");

        SwarmElasticProfileConfiguration expectedProperties = new SwarmElasticProfileConfiguration();
        expectedProperties.setImage("alpine:latest");
        expectedProperties.setMaxMemory("1G");
        assertThat(request.getElasticProfileConfiguration()).isEqualTo(expectedProperties);

    }
}
