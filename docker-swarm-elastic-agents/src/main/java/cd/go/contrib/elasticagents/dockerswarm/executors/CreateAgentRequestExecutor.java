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

import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.validator.DockerMountsValidator;
import cd.go.contrib.elasticagents.dockerswarm.validator.DockerSecretValidator;
import cd.go.contrib.elasticagents.dockerswarm.validator.Validatable;
import cd.go.plugin.base.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static cd.go.plugin.base.GsonTransformer.toJson;
import static java.text.MessageFormat.format;

public class CreateAgentRequestExecutor extends BaseExecutor<CreateAgentRequest> {
    private static final DateTimeFormatter MESSAGE_PREFIX_FORMATTER = DateTimeFormat.forPattern("'##|'HH:mm:ss.SSS '[go]'");
    private final ElasticAgentRequestClient pluginRequest;
    private List<Validatable<CreateAgentRequest>> validators = new ArrayList<>();

    public CreateAgentRequestExecutor(Map<String, DockerServices> clusterToServicesMap,
                                      ElasticAgentRequestClient pluginRequest) {
        super(clusterToServicesMap);
        this.pluginRequest = pluginRequest;
        validators.add(new DockerSecretValidator());
        validators.add(new DockerMountsValidator());
    }

    @Override
    protected GoPluginApiResponse execute(CreateAgentRequest request) {
        try {
            ConsoleLogAppender consoleLogAppender = text -> {
                final String message = String.format("%s %s\n", LocalTime.now().toString(MESSAGE_PREFIX_FORMATTER), text);
                pluginRequest.appendToConsoleLog(request.getJobIdentifier(), message);
            };
            refreshInstancesForCluster(request.getClusterProfileProperties());
            LOG.debug(format("[create-agent] Validating with profile: {0}", request.getElasticProfileConfiguration()));
            ValidationResult result = new ValidationResult();
            for (Validatable<CreateAgentRequest> validatable : validators) {
                result.merge(validatable.validate(request));
            }
            if (!result.isEmpty()) {
                LOG.debug(format("[create-agent] Error in validtion: {0}", result));
                pluginRequest.addServerHealthMessage(result);
                return DefaultGoPluginApiResponse.incompleteRequest(toJson(result));
            }
            LOG.debug(format("[create-agent] Creating agent with profile: {0}", request.getElasticProfileConfiguration()));

            DockerServices dockerServices = clusterToServicesMap.get(request.getClusterProfileProperties().uuid());
            dockerServices.create(request, pluginRequest, consoleLogAppender);

            LOG.debug(format("[create-agent] Done creating agent for profile: {0}", request.getElasticProfileConfiguration()));
            return new DefaultGoPluginApiResponse(200);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CreateAgentRequest parseRequest(String requestBody) {
        return fromJson(requestBody, CreateAgentRequest.class);
    }
}
