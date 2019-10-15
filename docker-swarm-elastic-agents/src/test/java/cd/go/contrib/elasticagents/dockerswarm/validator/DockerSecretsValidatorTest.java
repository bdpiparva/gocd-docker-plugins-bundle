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

package cd.go.contrib.elasticagents.dockerswarm.validator;

import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.ElasticProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import cd.go.plugin.base.validation.ValidationResult;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DockerSecretsValidatorTest {
    private DockerClientFactory dockerClientFactory;
    private CreateAgentRequest createAgentRequest;
    private DockerClient dockerClient;
    private ElasticProfileConfiguration elasticProfileConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        dockerClientFactory = mock(DockerClientFactory.class);
        ClusterProfileProperties clusterProfileProperties = mock(ClusterProfileProperties.class);
        dockerClient = mock(DockerClient.class);

        elasticProfileConfiguration = new ElasticProfileConfiguration();
        elasticProfileConfiguration.setImage("alpine");
        elasticProfileConfiguration.setSecrets("src=Foo");
        createAgentRequest = new CreateAgentRequest();
        createAgentRequest.setElasticProfileConfiguration(elasticProfileConfiguration)
                .setClusterProfileProperties(clusterProfileProperties);

        when(dockerClientFactory.docker(clusterProfileProperties)).thenReturn(dockerClient);
    }

    @Test
    void shouldValidateValidSecretConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final Secret secret = mock(Secret.class);


        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listSecrets()).thenReturn(of(secret));
        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Foo").build());
        when(secret.id()).thenReturn("service-id");

        ValidationResult validationResult = new DockerSecretValidator(dockerClientFactory).validate(createAgentRequest);

        assertThat(validationResult.isEmpty()).isTrue();
    }

    @Test
    void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final Version version = mock(Version.class);
        elasticProfileConfiguration.setSecrets("Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClientFactory.docker(any(ClusterProfileProperties.class))).thenReturn(dockerClient);

        ValidationResult validationResult = new DockerSecretValidator(null).validate(createAgentRequest);

        assertThat(validationResult.isEmpty()).isFalse();
        assertThat(validationResult.find("Secrets").get().getMessage()).isEqualTo("Invalid secret specification `Foo`. Must specify property `src` with value.");
    }

}
