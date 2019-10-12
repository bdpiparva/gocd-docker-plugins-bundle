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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import cd.go.contrib.elasticagents.dockerswarm.AgentInstances;
import cd.go.contrib.elasticagents.dockerswarm.DockerService;
import cd.go.contrib.elasticagents.dockerswarm.PluginRequest;
import cd.go.contrib.elasticagents.dockerswarm.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.validator.DockerMountsValidator;
import cd.go.contrib.elasticagents.dockerswarm.validator.DockerSecretValidator;
import cd.go.contrib.elasticagents.dockerswarm.validator.Validatable;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.DockerPlugin.LOG;
import static java.text.MessageFormat.format;

public class CreateAgentRequestExecutor implements RequestExecutor {
    private final AgentInstances<DockerService> agentInstances;
    private final PluginRequest pluginRequest;
    private final CreateAgentRequest request;
    private List<Validatable> validators = new ArrayList<>();

    public CreateAgentRequestExecutor(CreateAgentRequest request, AgentInstances<DockerService> agentInstances, PluginRequest pluginRequest) {
        this.request = request;
        this.agentInstances = agentInstances;
        this.pluginRequest = pluginRequest;
        validators.add(new DockerSecretValidator(request));
        validators.add(new DockerMountsValidator(request));
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        LOG.debug(format("[create-agent] Validating with profile: {0}", request.properties()));
        boolean hasError = false;
        List<Map<String, String>> messages = new ArrayList<>();
        for (Validatable validatable : validators) {
            ValidationResult validationResult = validatable.validate(request.properties());
            if (validationResult.hasErrors()) {
                hasError = true;
                Map<String, String> messageToBeAdded = new HashMap<>();
                messageToBeAdded.put("type", "warning");
                messageToBeAdded.put("message", validationResult.toJSON());
                messages.add(messageToBeAdded);
            }
        }
        if (hasError) {
            LOG.debug(format("[create-agent] Error in validtion: {0}", messages));
            pluginRequest.addServerHealthMessage(messages);
            return DefaultGoPluginApiResponse.incompleteRequest(messages.toString());
        }
        LOG.debug(format("[create-agent] Creating agent with profile: {0}", request.properties()));

        agentInstances.create(request, pluginRequest);

        LOG.debug(format("[create-agent] Done creating agent for profile: {0}", request.properties()));
        return new DefaultGoPluginApiResponse(200);
    }

}
