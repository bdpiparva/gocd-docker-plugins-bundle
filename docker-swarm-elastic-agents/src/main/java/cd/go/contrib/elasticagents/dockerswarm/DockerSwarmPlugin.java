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

package cd.go.contrib.elasticagents.dockerswarm;

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.dockerswarm.executors.*;
import cd.go.plugin.base.dispatcher.BaseBuilder;
import cd.go.plugin.base.dispatcher.RequestDispatcher;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.PLUGIN_IDENTIFIER;
import static cd.go.contrib.elasticagents.dockerswarm.Constants.PROCESSOR_API_VERSION;

@Extension
public class DockerSwarmPlugin implements GoPlugin {
    public static final Logger LOG = Logger.getLoggerFor(DockerSwarmPlugin.class);
    private RequestDispatcher requestDispatcher;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        final ElasticAgentRequestClient pluginRequest = new ElasticAgentRequestClient(accessor, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        final Map<String, DockerServices> clusterToServicesMap = new HashMap<>();
        requestDispatcher = BaseBuilder
                .forElastic()
                .v5()
                .icon("/docker-swarm/icon.png", "image/png")
                .capabilities(false, true, true)
                .clusterProfileMetadata(ClusterProfileProperties.class)
                .clusterProfileView("/docker-swarm/cluster-profile.template.html")
                .elasticProfileMetadata(ElasticProfileConfiguration.class)
                .elasticProfileView("/docker-swarm/elastic-profile.template.html")
//                .validateElasticProfile()
                .pluginStatusReport(null)
                .agentStatusReport(new AgentStatusReportExecutor())
                .clusterStatusReport(new ClusterStatusReportExecutor())
                .migrateConfiguration(new MigrateConfigurationRequestExecutor())
                .jobCompletion(new JobCompletionRequestExecutor(clusterToServicesMap, pluginRequest))
                .createAgent(new CreateAgentRequestExecutor(clusterToServicesMap, pluginRequest))
                .shouldAssignWork(new ShouldAssignWorkRequestExecutor(clusterToServicesMap))
                .serverPing(new ServerPingRequestExecutor(clusterToServicesMap, pluginRequest))
                .build();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        try {
            return requestDispatcher.dispatch(request);
        } catch (PluginSettingsNotConfiguredException e) {
            LOG.warn("Failed to handle request " + request.requestName() + " due to: " + e.getMessage());
            return DefaultGoPluginApiResponse.error("Failed to handle request " + request.requestName() + " due to:" + e.getMessage());
        } catch (Exception e) {
            LOG.error("Failed to handle request " + request.requestName() + " due to:", e);
            return DefaultGoPluginApiResponse.error("Failed to handle request " + request.requestName() + " due to:" + e.getMessage());
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

}
