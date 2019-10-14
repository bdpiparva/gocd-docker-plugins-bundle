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

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@ToString
@EqualsAndHashCode
public class Agents {

    private final Map<String, Agent> agents = new HashMap<>();

    // Filter for agents that can be disabled safely
    private static final Predicate<Agent> AGENT_IDLE_PREDICATE = metadata -> {
        AgentState agentState = metadata.agentState();
        return metadata.configState().equals(AgentConfigState.Enabled) && (agentState.equals(AgentState.Idle) || agentState.equals(AgentState.Missing) || agentState.equals(AgentState.LostContact));
    };

    // Filter for agents that can be terminated safely
    private static final Predicate<Agent> AGENT_DISABLED_PREDICATE = metadata -> {
        AgentState agentState = metadata.agentState();
        return metadata.configState().equals(AgentConfigState.Disabled) && (agentState.equals(AgentState.Idle) || agentState.equals(AgentState.Missing) || agentState.equals(AgentState.LostContact));
    };

    public Agents() {
    }

    public Agents(Collection<Agent> toCopy) {
        addAll(toCopy);
    }

    public void addAll(Collection<Agent> toAdd) {
        for (Agent agent : toAdd) {
            add(agent);
        }
    }

    public void addAll(Agents agents) {
        addAll(agents.agents());
    }

    public Collection<Agent> findInstancesToDisable() {
        return agents.values().stream().filter(AGENT_IDLE_PREDICATE).collect(toList());
    }

    public Collection<Agent> findInstancesToTerminate() {
        return agents.values().stream().filter(AGENT_DISABLED_PREDICATE).collect(toList());
    }

    public Set<String> agentIds() {
        return new LinkedHashSet<>(agents.keySet());
    }

    public boolean containsAgentWithId(String agentId) {
        return agents.containsKey(agentId);
    }

    public Collection<Agent> agents() {
        return new ArrayList<>(agents.values());
    }

    public void add(Agent agent) {
        agents.put(agent.elasticAgentId(), agent);
    }

}
