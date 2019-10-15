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

import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import cd.go.contrib.elasticagents.common.requests.AbstractCreateAgentRequest;
import cd.go.contrib.elasticagents.dockerswarm.utils.Size;
import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.*;
import static cd.go.contrib.elasticagents.dockerswarm.DockerSwarmPlugin.LOG;
import static cd.go.plugin.base.GsonTransformer.fromJson;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DockerService {
    private static final Gson GSON = new Gson();
    private final DateTime createdAt;
    private final SwarmElasticProfileConfiguration properties;
    private final String environment;
    private JobIdentifier jobIdentifier;
    private String name;

    public DockerService(String name,
                         Date createdAt,
                         SwarmElasticProfileConfiguration properties,
                         String environment,
                         JobIdentifier jobIdentifier) {
        this.name = name;
        this.createdAt = new DateTime(createdAt);
        this.properties = properties;
        this.environment = environment;
        this.jobIdentifier = jobIdentifier;
    }

    public String name() {
        return name;
    }

    public DateTime createdAt() {
        return createdAt;
    }

    public String environment() {
        return environment;
    }

    public SwarmElasticProfileConfiguration getElasticProfileConfiguration() {
        return properties;
    }

    public JobIdentifier jobIdentifier() {
        return jobIdentifier;
    }

    public void terminate(DockerClient docker) throws DockerException, InterruptedException {
        try {
            LOG.debug("Terminating service " + this.name());
            docker.removeService(name);
        } catch (ServiceNotFoundException ignore) {
            LOG.warn("Cannot terminate a service that does not exist " + name);
        }
    }

    public static DockerService fromService(Service service) {
        Map<String, String> labels = service.spec().labels();
        final SwarmElasticProfileConfiguration properties = fromJson(labels.get(CONFIGURATION_LABEL_KEY), SwarmElasticProfileConfiguration.class);
        return new DockerService(service.spec().name(),
                service.createdAt(),
                properties,
                labels.get(ENVIRONMENT_LABEL_KEY),
                JobIdentifier.fromJson(labels.get(JOB_IDENTIFIER_LABEL_KEY)));
    }

    public static DockerService create(AbstractCreateAgentRequest<SwarmElasticProfileConfiguration, SwarmClusterConfiguration> request,
                                       SwarmClusterConfiguration swarmClusterConfiguration,
                                       DockerClient docker) throws InterruptedException, DockerException {
        String serviceName = UUID.randomUUID().toString();

        HashMap<String, String> labels = labelsFrom(request);
        String imageName = image(request.getElasticProfileConfiguration().getImage());
        String[] env = environmentFrom(request, swarmClusterConfiguration, serviceName);

        final ContainerSpec.Builder containerSpecBuilder = ContainerSpec.builder()
                .image(imageName)
                .env(env);

        if (StringUtils.isNotBlank(request.getElasticProfileConfiguration().getCommand())) {
            containerSpecBuilder.command(Util.splitIntoLinesAndTrimSpaces(request.getElasticProfileConfiguration().getCommand()).toArray(new String[]{}));
        }

        if (Util.dockerApiVersionAtLeast(docker, "1.26")) {
            containerSpecBuilder.hosts(new Hosts().hosts(request.getElasticProfileConfiguration().getHosts()));
            final DockerMounts dockerMounts = DockerMounts.fromString(request.getElasticProfileConfiguration().getMounts());
            containerSpecBuilder.mounts(dockerMounts.toMount());
            final DockerSecrets dockerSecrets = DockerSecrets.fromString(request.getElasticProfileConfiguration().getSecrets());
            containerSpecBuilder.secrets(dockerSecrets.toSecretBind(docker.listSecrets()));
        } else {
            LOG.warn(format("Detected docker version and api version is {0} and {1} respectively. Docker with api version 1.26 or above is required to use volume mounts, secrets and host file entries. Please refer https://docs.docker.com/engine/api/v1.32/#section/Versioning for more information about docker release.", docker.version().version(), docker.version().apiVersion()));
        }

        Driver.Builder driverBuilder = Driver.builder()
                .name(request.getElasticProfileConfiguration().getLogDriver())
                .options(Util.linesToMap(request.getElasticProfileConfiguration().getLogDriverOptions()));

        TaskSpec taskSpec = TaskSpec.builder()
                .containerSpec(containerSpecBuilder.build())
                .resources(resourceRequirements(request.getElasticProfileConfiguration()))
                .placement(Placement.create(Util.linesToList(request.getElasticProfileConfiguration().getConstraints())))
                .logDriver(StringUtils.isBlank(request.getElasticProfileConfiguration().getLogDriver()) ? null : driverBuilder.build())
                .build();

        ServiceSpec serviceSpec = ServiceSpec.builder()
                .name(serviceName)
                .labels(labels)
                .taskTemplate(taskSpec)
                .networks(Networks.fromString(request.getElasticProfileConfiguration().getNetworks(), docker.listNetworks()))
                .build();

        ServiceCreateResponse service = docker.createService(serviceSpec);

        String id = service.id();

        Service serviceInfo = docker.inspectService(id);

        LOG.debug("Created service " + serviceInfo.spec().name());
        return new DockerService(serviceName,
                serviceInfo.createdAt(),
                request.getElasticProfileConfiguration(),
                request.getEnvironment(),
                request.getJobIdentifier());
    }

    private static ResourceRequirements resourceRequirements(SwarmElasticProfileConfiguration swarmElasticProfileConfiguration) {
        ResourceRequirements.Builder resourceRequirementsBuilder = ResourceRequirements.builder();
        final String maxMemory = swarmElasticProfileConfiguration.getMaxMemory();
        if (StringUtils.isNotBlank(maxMemory)) {
            resourceRequirementsBuilder.limits(
                    Resources.builder()
                            .memoryBytes(Size.parse(maxMemory).toBytes())
                            .build()
            );
        }

        final String reservedMemory = swarmElasticProfileConfiguration.getReservedMemory();
        if (StringUtils.isNotBlank(reservedMemory)) {
            resourceRequirementsBuilder.reservations(
                    Resources.builder()
                            .memoryBytes(Size.parse(reservedMemory).toBytes())
                            .build()
            );
        }

        return resourceRequirementsBuilder.build();
    }

    private static String[] environmentFrom(AbstractCreateAgentRequest<SwarmElasticProfileConfiguration, SwarmClusterConfiguration> request,
                                            SwarmClusterConfiguration swarmClusterConfiguration,
                                            String containerName) {
        Set<String> env = new HashSet<>();

        env.addAll(swarmClusterConfiguration.getEnvironmentVariables());
        if (StringUtils.isNotBlank(request.getElasticProfileConfiguration().getEnvironment())) {
            env.addAll(Util.splitIntoLinesAndTrimSpaces(request.getElasticProfileConfiguration().getEnvironment()));
        }

        env.addAll(Arrays.asList(
                "GO_EA_MODE=" + mode(),
                "GO_EA_SERVER_URL=" + swarmClusterConfiguration.getGoServerUrl(),
                "GO_EA_GUID=" + "docker-swarm." + containerName
        ));

        env.addAll(request.autoregisterPropertiesAsEnvironmentVars(containerName, PLUGIN_ID));

        return env.toArray(new String[env.size()]);
    }

    private static HashMap<String, String> labelsFrom(AbstractCreateAgentRequest<SwarmElasticProfileConfiguration, SwarmClusterConfiguration> request) {
        HashMap<String, String> labels = new HashMap<>();

        labels.put(CREATED_BY_LABEL_KEY, Constants.PLUGIN_ID);
        labels.put(JOB_IDENTIFIER_LABEL_KEY, request.getJobIdentifier().toJson());
        if (StringUtils.isNotBlank(request.getEnvironment())) {
            labels.put(ENVIRONMENT_LABEL_KEY, request.getEnvironment());
        }
        labels.put(CONFIGURATION_LABEL_KEY, GSON.toJson(request.getElasticProfileConfiguration()));
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerService that = (DockerService) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    private static String mode() {
        if ("false".equals(System.getProperty("rails.use.compressed.js"))) {
            return "dev";
        }

        if ("true".equalsIgnoreCase(System.getProperty("rails.use.compressed.js"))) {
            return "prod";
        }

        return "";
    }

    private static String image(String image) {
        if (isBlank(image)) {
            throw new IllegalArgumentException("Must provide `Image` attribute.");
        }

        if (!image.contains(":")) {
            return image + ":latest";
        }
        return image;
    }

}
