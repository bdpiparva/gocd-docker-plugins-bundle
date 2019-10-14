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

import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.models.ClusterProfileConfiguration;
import cd.go.contrib.elasticagents.common.models.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.common.requests.AbstractCreateAgentRequest;

/**
 * Plugin implementors should implement these methods to interface to your cloud.
 * This interface is merely a suggestion for a very simple plugin. You may change it to your needs.
 */
public interface AgentInstances<T, E extends ElasticProfileConfiguration, C extends ClusterProfileConfiguration> {
    /**
     * This message is sent to request creation of an agent instance.
     * Implementations may, at their discretion choose to not spin up an agent instance.
     * <p>
     * So that instances created are auto-registered with the server, the agent instance MUST have an
     * <code>autoregister.properties</code> file.
     *
     * @param request            the request object
     * @param pluginRequest      the plugin request object
     * @param consoleLogAppender appender for console log
     */
    T create(AbstractCreateAgentRequest<E, C> request,
             ElasticAgentRequestClient pluginRequest,
             ConsoleLogAppender consoleLogAppender) throws Exception;

    /**
     * This message is sent when the plugin needs to terminate the agent instance.
     *
     * @param agentId                     the elastic agent id
     * @param clusterProfileConfiguration the cluster profile properties object
     */
    void terminate(String agentId, C clusterProfileConfiguration) throws Exception;

    /**
     * This message is to terminate instances that did not register with the server
     * after a timeout. The timeout may be configurable and set via the
     * {@link ElasticAgentRequestClient} instance that is passed in.
     *
     * @param clusterProfileConfiguration the cluster profile properties object
     * @param agents                      the list of all the agents
     */
    void terminateUnregisteredInstances(C clusterProfileConfiguration,
                                        Agents agents) throws Exception;

    /**
     * This message to filter out any new agents, that have registered before the timeout period.
     * The timeout may be configurable and set via the {@link ElasticAgentRequestClient} instance
     * that is passed in.
     *
     * @param clusterProfileConfiguration the cluster profile properties object
     * @param agents                      the list of all the agents
     * @return a list of agent instances which were created after auto register period ago.
     */
    Agents instancesCreatedAfterTimeout(C clusterProfileConfiguration, Agents agents);

    /**
     * This message is sent after plugin initialization time so that the plugin may connect to the cloud provider
     * and fetch a list of all instances that have been spun up by this plugin (before the server was shut down).
     * This call should be should ideally remember if the agent instances are refreshed from the cluster,
     * and do nothing if instances were previously refreshed.
     *
     * @param clusterProfileConfiguration the list of cluster profile properties
     */
    void refreshAll(C clusterProfileConfiguration) throws Exception;

    /**
     * This
     * Returns an agent instance with the specified <code>id</code> or <code>null</code>, if the agent is not found.
     *
     * @param agentId the elastic agent id
     */
    T find(String agentId);
}
