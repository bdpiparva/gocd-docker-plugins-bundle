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
import cd.go.contrib.elasticagents.common.agent.Agents;
import cd.go.contrib.elasticagents.common.exceptions.ServerRequestFailedException;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.plugin.base.validation.ValidationResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Instances of this class know how to send messages to the GoCD Server.
 */
public class ElasticAgentRequestClient {
    private static final Logger LOG = Logger.getLoggerFor(ElasticAgentRequestClient.class);
    private static final String CONSOLE_LOG_API_VERSION = "1.0";
    private static final String REQUEST_SERVER_DISABLE_AGENT = "go.processor.elastic-agents.disable-agents";
    private static final String REQUEST_SERVER_DELETE_AGENT = "go.processor.elastic-agents.delete-agents";
    private static final String REQUEST_SERVER_LIST_AGENTS = "go.processor.elastic-agents.list-agents";
    private static final String REQUEST_SERVER_SERVER_HEALTH_ADD_MESSAGES = "go.processor.server-health.add-messages";
    private static final String REQUEST_SERVER_APPEND_TO_CONSOLE_LOG = "go.processor.console-log.append";

    private String processorApiVersion;
    private GoPluginIdentifier pluginIdentifier;

    private final GoApplicationAccessor accessor;

    public ElasticAgentRequestClient(GoApplicationAccessor accessor,
                                     String processorApiVersion,
                                     GoPluginIdentifier pluginIdentifier) {
        this.accessor = accessor;
        this.processorApiVersion = processorApiVersion;
        this.pluginIdentifier = pluginIdentifier;
    }

    public Agents listAgents() throws ServerRequestFailedException {
        DefaultGoApiRequest request = new DefaultGoApiRequest(REQUEST_SERVER_LIST_AGENTS, processorApiVersion, pluginIdentifier);
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.listAgents(response);
        }

        return new Agents(Agent.fromJSONArray(response.responseBody()));
    }

    public void disableAgents(Collection<Agent> toBeDisabled) throws ServerRequestFailedException {
        if (toBeDisabled.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(REQUEST_SERVER_DISABLE_AGENT, processorApiVersion, pluginIdentifier);
        request.setRequestBody(Agent.toJSONArray(toBeDisabled));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.disableAgents(response);
        }
    }

    public void deleteAgents(Collection<Agent> toBeDeleted) throws ServerRequestFailedException {
        if (toBeDeleted.isEmpty()) {
            return;
        }

        DefaultGoApiRequest request = new DefaultGoApiRequest(REQUEST_SERVER_DELETE_AGENT, processorApiVersion, pluginIdentifier);
        request.setRequestBody(Agent.toJSONArray(toBeDeleted));
        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            throw ServerRequestFailedException.deleteAgents(response);
        }
    }

    public void addServerHealthMessage(List<Map<String, String>> messages) {
        Gson gson = new Gson();

        DefaultGoApiRequest request = new DefaultGoApiRequest(REQUEST_SERVER_SERVER_HEALTH_ADD_MESSAGES, processorApiVersion, pluginIdentifier);

        request.setRequestBody(gson.toJson(messages));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("The server sent an unexpected status code " + response.responseCode() + " with the response body " + response.responseBody());
        }
    }

    public void appendToConsoleLog(JobIdentifier jobIdentifier, String text) throws ServerRequestFailedException {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("pipelineName", jobIdentifier.getPipelineName());
        requestMap.put("pipelineCounter", String.valueOf(jobIdentifier.getPipelineCounter()));
        requestMap.put("stageName", jobIdentifier.getStageName());
        requestMap.put("stageCounter", jobIdentifier.getStageCounter());
        requestMap.put("jobName", jobIdentifier.getJobName());
        requestMap.put("text", text);

        DefaultGoApiRequest request = new DefaultGoApiRequest(REQUEST_SERVER_APPEND_TO_CONSOLE_LOG, CONSOLE_LOG_API_VERSION, pluginIdentifier);
        request.setRequestBody(new GsonBuilder().create().toJson(requestMap));

        GoApiResponse response = accessor.submit(request);

        if (response.responseCode() != 200) {
            LOG.error("Failed to append to console log for " + jobIdentifier.represent() + " with text: " + text);
        }
    }

    public void addServerHealthMessage(ValidationResult result) {
        addServerHealthMessage(result.stream()
                .map(error -> Map.of("type", "warning", "message", error.getMessage()))
                .collect(Collectors.toList()));
    }
}
