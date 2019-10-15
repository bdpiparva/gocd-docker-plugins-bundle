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

import cd.go.contrib.elasticagents.common.Clock;
import cd.go.contrib.elasticagents.common.ConsoleLogAppender;
import cd.go.contrib.elasticagents.common.ElasticAgentRequestClient;
import cd.go.contrib.elasticagents.common.SetupSemaphore;
import cd.go.contrib.elasticagents.common.agent.Agent;
import cd.go.contrib.elasticagents.common.agent.AgentInstances;
import cd.go.contrib.elasticagents.common.agent.Agents;
import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.common.requests.AbstractCreateAgentRequest;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.swarm.Service;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class DockerServices implements AgentInstances<DockerService, SwarmElasticProfileConfiguration, SwarmClusterConfiguration> {
    private final ConcurrentHashMap<String, DockerService> services = new ConcurrentHashMap<>();
    private final DockerClientFactory factory;
    private boolean refreshed;
    private List<JobIdentifier> jobsWaitingForAgentCreation = new ArrayList<>();
    public Clock clock = Clock.DEFAULT;
    final Semaphore semaphore = new Semaphore(0, true);

    public DockerServices() {
        this(DockerClientFactory.instance());
    }

    //Only for the test
    public DockerServices(DockerClientFactory factory) {
        this.factory = factory;
    }

    @Override
    public DockerService create(AbstractCreateAgentRequest<SwarmElasticProfileConfiguration, SwarmClusterConfiguration> request,
                                ElasticAgentRequestClient pluginRequest,
                                ConsoleLogAppender consoleLogAppender) throws Exception {
        SwarmClusterConfiguration swarmClusterConfiguration = request.getClusterProfileProperties();
        final Integer maxAllowedContainers = swarmClusterConfiguration.getMaxDockerContainers();
        synchronized (services) {
            if (!jobsWaitingForAgentCreation.contains(request.getJobIdentifier())) {
                jobsWaitingForAgentCreation.add(request.getJobIdentifier());
            }
            doWithLockOnSemaphore(new SetupSemaphore(maxAllowedContainers, services, semaphore));
            List<Map<String, String>> messages = new ArrayList<>();

            if (semaphore.tryAcquire()) {
                pluginRequest.addServerHealthMessage(messages);
                DockerService dockerService = DockerService.create(request, swarmClusterConfiguration, docker(swarmClusterConfiguration));
                register(dockerService);
                jobsWaitingForAgentCreation.remove(request.getJobIdentifier());
                return dockerService;
            } else {
                String maxLimitExceededMessage = "The number of containers currently running is currently at the maximum permissible limit (" + services.size() + "). Not creating any more containers.";
                Map<String, String> messageToBeAdded = new HashMap<>();
                messageToBeAdded.put("type", "warning");
                messageToBeAdded.put("message", maxLimitExceededMessage);
                messages.add(messageToBeAdded);
                pluginRequest.addServerHealthMessage(messages);
                DockerSwarmPlugin.LOG.info(maxLimitExceededMessage);
                return null;
            }
        }
    }

    private void doWithLockOnSemaphore(Runnable runnable) {
        synchronized (semaphore) {
            runnable.run();
        }
    }

    @Override
    public void terminate(String agentId, SwarmClusterConfiguration swarmClusterConfiguration) throws Exception {
        DockerService instance = services.get(agentId);
        if (instance != null) {
            instance.terminate(docker(swarmClusterConfiguration));
        } else {
            DockerSwarmPlugin.LOG.warn("Requested to terminate an instance that does not exist " + agentId);
        }

        doWithLockOnSemaphore(new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        });

        synchronized (services) {
            services.remove(agentId);
        }
    }

    @Override
    public void terminateUnregisteredInstances(SwarmClusterConfiguration swarmClusterConfiguration,
                                               Agents agents) throws Exception {
        DockerServices toTerminate = unregisteredAfterTimeout(swarmClusterConfiguration, agents);
        if (toTerminate.services.isEmpty()) {
            return;
        }

        DockerSwarmPlugin.LOG.warn("Terminating services that did not register " + toTerminate.services.keySet());
        for (DockerService dockerService : toTerminate.services.values()) {
            terminate(dockerService.name(), swarmClusterConfiguration);
        }
    }

    @Override
    public Agents instancesCreatedAfterTimeout(SwarmClusterConfiguration settings, Agents agents) {
        ArrayList<Agent> oldAgents = new ArrayList<>();
        for (Agent agent : agents.agents()) {
            DockerService instance = services.get(agent.elasticAgentId());
            if (instance == null) {
                continue;
            }

            if (clock.now().isAfter(instance.createdAt().plus(settings.getAutoRegisterPeriod()))) {
                oldAgents.add(agent);
            }
        }
        return new Agents(oldAgents);
    }

    private void refreshAgentInstances(SwarmClusterConfiguration pluginSettings) throws Exception {
        DockerClient dockerClient = docker(pluginSettings);
        List<Service> clusterSpecificServices = dockerClient.listServices();
        services.clear();
        for (Service service : clusterSpecificServices) {
            ImmutableMap<String, String> labels = service.spec().labels();
            if (labels != null && Constants.PLUGIN_ID.equals(labels.get(Constants.CREATED_BY_LABEL_KEY))) {
                register(DockerService.fromService(service));
            }
        }
        refreshed = true;
    }

    //TODO: Delete this
    public void refreshAll(SwarmClusterConfiguration swarmClusterConfiguration, boolean forceRefresh) throws Exception {
        if (!refreshed || forceRefresh) {
            refreshAgentInstances(swarmClusterConfiguration);
        }
    }

    @Override
    public void refreshAll(SwarmClusterConfiguration pluginSettings) throws Exception {
        if (!refreshed) {
            refreshAgentInstances(pluginSettings);
        }
    }

    public void register(DockerService service) {
        services.put(service.name(), service);
    }

    private DockerClient docker(SwarmClusterConfiguration swarmClusterConfiguration) throws Exception {
        return factory.docker(swarmClusterConfiguration);
    }

    private DockerServices unregisteredAfterTimeout(SwarmClusterConfiguration swarmClusterConfiguration,
                                                    Agents knownAgents) throws Exception {
        Period period = swarmClusterConfiguration.getAutoRegisterPeriod();
        DockerServices unregisteredContainers = new DockerServices();

        for (String serviceName : services.keySet()) {
            if (knownAgents.containsAgentWithId(serviceName)) {
                continue;
            }

            Service serviceInfo;
            try {
                serviceInfo = docker(swarmClusterConfiguration).inspectService(serviceName);
            } catch (ServiceNotFoundException e) {
                DockerSwarmPlugin.LOG.warn("The container " + serviceName + " could not be found.");
                continue;
            }
            DateTime dateTimeCreated = new DateTime(serviceInfo.createdAt());

            if (clock.now().isAfter(dateTimeCreated.plus(period))) {
                unregisteredContainers.register(DockerService.fromService(serviceInfo));
            }
        }
        return unregisteredContainers;
    }

    public boolean hasInstance(String agentId) {
        return services.containsKey(agentId);
    }

    @Override
    public DockerService find(String agentId) {
        return services.get(agentId);
    }

    // used by test
    protected boolean isEmpty() {
        return services.isEmpty();
    }

}
