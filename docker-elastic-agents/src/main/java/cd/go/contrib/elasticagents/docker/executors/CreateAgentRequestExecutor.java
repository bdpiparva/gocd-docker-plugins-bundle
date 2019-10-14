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

package cd.go.contrib.elasticagents.docker.executors;

import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.requests.AbstractCreateAgentRequest;
import cd.go.contrib.elasticagents.docker.DockerContainers;
import cd.go.contrib.elasticagents.docker.requests.CreateAgentRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public class CreateAgentRequestExecutor extends BaseExecutor<CreateAgentRequest> {
    private static final DateTimeFormatter MESSAGE_PREFIX_FORMATTER = DateTimeFormat.forPattern("'##|'HH:mm:ss.SSS '[go]'");
    private final ElasticAgentRequestClient pluginRequest;

    public CreateAgentRequestExecutor(Map<String, DockerContainers> clusterToContainerMap,
                                      ElasticAgentRequestClient pluginRequest) {
        super(clusterToContainerMap);
        this.pluginRequest = pluginRequest;
    }

    @Override
    protected GoPluginApiResponse execute(CreateAgentRequest request) {
        ConsoleLogAppender consoleLogAppender = text -> {
            final String message = String.format("%s %s\n", LocalTime.now().toString(MESSAGE_PREFIX_FORMATTER), text);
            pluginRequest.appendToConsoleLog(request.getJobIdentifier(), message);
        };

        consoleLogAppender.accept(String.format("Received request to create a container of %s at %s", request.getElasticProfileConfiguration().getImage(), new DateTime().toString("yyyy-MM-dd HH:mm:ss ZZ")));

        try {
            refreshInstancesForCluster(request.getClusterProfileProperties());
            DockerContainers dockerContainers = clusterToContainersMap.get(request.getClusterProfileProperties().uuid());
            dockerContainers.create(request, pluginRequest, consoleLogAppender);
        } catch (Exception e) {
            consoleLogAppender.accept(String.format("Failed while creating container: %s", e.getMessage()));
            throw new RuntimeException(e);
        }

        return new DefaultGoPluginApiResponse(200);
    }

    @Override
    protected CreateAgentRequest parseRequest(String requestBody) {
        return CreateAgentRequest.fromJSON(requestBody, CreateAgentRequest.class);
    }
}
