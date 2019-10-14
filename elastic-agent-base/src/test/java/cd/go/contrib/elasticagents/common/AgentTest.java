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

package cd.go.contrib.elasticagents.common;

import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.AgentBuildState;
import cd.go.contrib.elasticagents.common.agent.AgentConfigState;
import cd.go.contrib.elasticagents.common.agent.AgentState;
import cd.go.plugin.base.test_helper.annotations.JsonSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentTest {
    @ParameterizedTest
    @JsonSource(jsonFiles = "/common/agents.json")
    void shouldSerializeToJSON(String expectedAgentsJSON) throws Exception {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);
        String agentsJSON = Agent.toJSONArray(List.of(agent));

        JSONAssert.assertEquals(expectedAgentsJSON, agentsJSON, true);
    }

    @Test
    void shouldDeserializeFromJSON() throws Exception {
        List<Agent> agents = Agent.fromJSONArray("[{\"agent_id\":\"eeb9e0eb-1f12-4366-a5a5-59011810273b\",\"agent_state\":\"Building\",\"build_state\":\"Cancelled\",\"config_state\":\"Disabled\"}]");
        assertThat(agents).hasSize(1);

        Agent agent = agents.get(0);

        assertThat(agent.elasticAgentId()).isEqualTo("eeb9e0eb-1f12-4366-a5a5-59011810273b");
        assertThat(agent.agentState()).isEqualTo(AgentState.Building);
        assertThat(agent.buildState()).isEqualTo(AgentBuildState.Cancelled);
        assertThat(agent.configState()).isEqualTo(AgentConfigState.Disabled);
    }

    @Test
    void agentsWithSameAttributesShouldBeEqual() throws Exception {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);
        Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);

        assertThat(agent1.equals(agent2)).isTrue();
    }

    @Test
    void agentShouldEqualItself() throws Exception {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);

        assertThat(agent.equals(agent)).isTrue();
    }

    @Test
    void agentShouldNotEqualAnotherAgentWithDifferentAttributes() throws Exception {
        Agent agent = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);

        assertThat(agent.equals(new Agent())).isFalse();
    }

    @Test
    void agentsWithSameAttributesShareSameHashCode() throws Exception {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);
        Agent agent2 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);

        assertThat(agent1.hashCode()).isEqualTo(agent2.hashCode());
    }

    @Test
    void agentsWithDifferentAttributesDoNotShareSameHashCode() throws Exception {
        Agent agent1 = new Agent("eeb9e0eb-1f12-4366-a5a5-59011810273b", AgentState.Building, AgentBuildState.Cancelled, AgentConfigState.Disabled);
        Agent agent2 = new Agent();

        assertThat(agent1.hashCode()).isNotEqualTo(agent2.hashCode());
    }
}

