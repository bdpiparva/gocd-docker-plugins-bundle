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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServerPingRequestTest {
    @Test
    void shouldDeserializeJSONBody() {
        String requestBody = "{\n " +
                " \"all_cluster_profile_properties\": [\n    " +
                "{\n      " +
                "      \"go_server_url\": \"foo\",\n" +
                "      \"max_docker_containers\": \"100\",\n" +
                "      \"docker_uri\": \"dockerURI\",\n" +
                "      \"auto_register_timeout\": \"1\",\n" +
                "      \"private_registry_password\": \"foobar\",\n" +
                "      \"enable_private_registry_authentication\": \"false\",\n" +
                "      \"private_registry_custom_credentials\": \"true\",\n" +
                "      \"pull_on_container_create\": \"false\"\n" +
                "    }\n" +
                "   ]" +
                "\n}";

        List<ClusterProfileProperties> allClusterProfileProperties = ServerPingRequest.fromJSON(requestBody).getAllClusterProfileProperties();
        assertThat(allClusterProfileProperties).hasSize(1);
    }
}
