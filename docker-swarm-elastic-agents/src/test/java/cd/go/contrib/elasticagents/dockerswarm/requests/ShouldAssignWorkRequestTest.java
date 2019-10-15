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

import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.AgentBuildState;
import cd.go.contrib.elasticagents.common.agent.AgentConfigState;
import cd.go.contrib.elasticagents.common.agent.AgentState;
import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.utils.JobIdentifierMother;
import cd.go.plugin.base.test_helper.annotations.JsonSource;
import org.junit.jupiter.params.ParameterizedTest;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.assertj.core.api.Assertions.assertThat;

class ShouldAssignWorkRequestTest {

    @ParameterizedTest
    @JsonSource(jsonFiles = "/docker-swarm/should-assign-work-request.json")
    void shouldDeserializeFromJSON(String inputJSON) {
        ShouldAssignWorkRequest request = fromJson(inputJSON, ShouldAssignWorkRequest.class);

        assertThat(request.getEnvironment()).isEqualTo("Production");
        assertThat(request.getAgent()).isEqualTo(new Agent("42", AgentState.Idle, AgentBuildState.Idle, AgentConfigState.Enabled));
        assertThat(request.getJobIdentifier()).isEqualTo(JobIdentifierMother.get());
        assertThat(request.getClusterProfileProperties()).isEqualTo(
                new ClusterProfileProperties()
                        .setGoServerUrl("https://go.server.url/go")
                        .setDockerURI("unix://foo/bar")
        );
        assertThat(request.getElasticProfileConfiguration()).isEqualTo(
                new ElasticProfileConfiguration()
                        .setImage("alpine:latest")
                        .setCommand("/bin/sleep\n3")
                        .setMaxMemory("10G")
                        .setReservedMemory("5G")
        );
    }
}
