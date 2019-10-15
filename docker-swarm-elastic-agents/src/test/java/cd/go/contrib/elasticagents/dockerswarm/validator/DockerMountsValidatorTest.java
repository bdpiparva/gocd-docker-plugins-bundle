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
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class DockerMountsValidatorTest {
    @Mock
    private DockerClientFactory dockerClientFactory;
    @Mock
    private DockerClient dockerClient;

    private CreateAgentRequest createAgentRequest;
    private DockerMountsValidator validator;
    private ElasticProfileConfiguration elasticProfileConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        initMocks(this);

        ClusterProfileProperties clusterProfileProperties = new ClusterProfileProperties();
        when(dockerClientFactory.docker(clusterProfileProperties)).thenReturn(dockerClient);

        elasticProfileConfiguration = new ElasticProfileConfiguration()
                .setImage("alpine:latest")
                .setMounts("src=Foo, target=Bar");

        createAgentRequest = new CreateAgentRequest();
        createAgentRequest.setClusterProfileProperties(clusterProfileProperties)
                .setElasticProfileConfiguration(elasticProfileConfiguration);
        validator = new DockerMountsValidator(dockerClientFactory);
    }

    @Test
    void shouldValidateValidVolumeMountConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final VolumeList volumeList = mock(VolumeList.class);

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listVolumes()).thenReturn(volumeList);
        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = validator.validate(createAgentRequest);

        assertThat(validationResult.isEmpty()).isTrue();
    }

    @Test
    void shouldValidateDockerApiVersionForDockerMountSupport() throws Exception {
        final Version version = mock(Version.class);

        when(version.apiVersion()).thenReturn("1.25");
        when(dockerClient.version()).thenReturn(version);

        ValidationResult validationResult = validator.validate(createAgentRequest);

        assertThat(validationResult.isEmpty()).isFalse();
        assertThat(validationResult.find("Mounts").get().getMessage()).isEqualTo("Docker volume mount requires api version 1.26 or higher.");
    }

    @Test
    void shouldValidateInvalidDockerSecretsConfiguration() throws Exception {
        final Version version = mock(Version.class);
        final VolumeList volumeList = mock(VolumeList.class);
        elasticProfileConfiguration.setMounts("src=Foo");

        when(version.apiVersion()).thenReturn("1.27");
        when(dockerClient.version()).thenReturn(version);
        when(dockerClient.listVolumes()).thenReturn(volumeList);
        when(volumeList.volumes()).thenReturn(new ImmutableList.Builder<Volume>().add(Volume.builder().name("Foo").build()).build());

        ValidationResult validationResult = validator.validate(createAgentRequest);

        assertThat(validationResult.isEmpty()).isFalse();
        assertThat(validationResult.find("Mounts").get().getMessage()).isEqualTo("Invalid mount target specification `src=Foo`. `target` has to be specified.");
    }
}
