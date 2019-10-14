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

package cd.go.contrib.elasticagents.docker;

import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.ViewBuilder;
import cd.go.contrib.elasticagents.docker.executors.*;
import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.docker.validator.ContainerMemorySettingsValidator;
import cd.go.contrib.elasticagents.docker.validator.CpusMetadataValidator;
import cd.go.contrib.elasticagents.docker.validator.GoServerURLValidator;
import cd.go.contrib.elasticagents.docker.validator.HostsValidator;
import cd.go.plugin.base.dispatcher.BaseBuilder;
import cd.go.plugin.base.dispatcher.RequestDispatcher;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.Constants.PLUGIN_IDENTIFIER;
import static cd.go.contrib.elasticagents.docker.Constants.PROCESSOR_API_VERSION;

@Extension
public class DockerPlugin implements GoPlugin {

    public static final Logger LOG = Logger.getLoggerFor(DockerPlugin.class);

    private RequestDispatcher requestDispatcher;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        final ElasticAgentRequestClient pluginRequest = new ElasticAgentRequestClient(accessor, PROCESSOR_API_VERSION, PLUGIN_IDENTIFIER);
        final Map<String, DockerContainers> clusterSpecificAgentInstances = new HashMap<>();
        requestDispatcher = BaseBuilder
                .forElastic()
                .v5()
                .icon("/docker/icon.svg", "image/svg+xml")
                .capabilities(false, true, true)
                .clusterProfileMetadata(ClusterProfileProperties.class)
                .clusterProfileView("/docker/cluster-profile.template.html")
                .elasticProfileMetadata(ElasticProfileConfiguration.class)
                .elasticProfileView("/docker/elastic-profile.template.html")
                .validateElasticProfile(
                        new GoServerURLValidator(),
                        new ContainerMemorySettingsValidator(),
                        new CpusMetadataValidator(),
                        new HostsValidator()
                )
                .pluginStatusReport(null)
                .agentStatusReport(new AgentStatusReportExecutor(clusterSpecificAgentInstances, ViewBuilder.instance()))
                .clusterStatusReport(new ClusterStatusReportExecutor(clusterSpecificAgentInstances, ViewBuilder.instance()))
                .migrateConfiguration(new MigrateConfigurationRequestExecutor())
                .jobCompletion(new JobCompletionRequestExecutor(clusterSpecificAgentInstances, pluginRequest))
                .createAgent(new CreateAgentRequestExecutor(clusterSpecificAgentInstances, pluginRequest))
                .shouldAssignWork(new ShouldAssignWorkRequestExecutor(clusterSpecificAgentInstances))
                .serverPing(new ServerPingRequestExecutor(clusterSpecificAgentInstances, pluginRequest))
                .build();

    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        try {
            return requestDispatcher.dispatch(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return PLUGIN_IDENTIFIER;
    }

}
