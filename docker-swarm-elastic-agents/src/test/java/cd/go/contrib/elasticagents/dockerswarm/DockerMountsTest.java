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

import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.mount.Mount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DockerMountsTest {

    @Test
    void shouldBuildVolumeMountFromString() {
        final DockerMounts mounts = DockerMounts.fromString("source=namedVolume, target=/path/in/container");

        assertThat(mounts).isNotNull();
        assertThat(mounts).hasSize(1);

        assertThat(mounts.get(0).type()).isEqualTo("volume");
        assertThat(mounts.get(0).source()).isEqualTo("namedVolume");
        assertThat(mounts.get(0).target()).isEqualTo("/path/in/container");
    }

    @Test
    void shouldBuildBindMountFromString() {
        final DockerMounts mounts = DockerMounts.fromString("type=bind, source=/path/in/host, target=/path/in/container");

        assertThat(mounts).isNotNull();
        assertThat(mounts).hasSize(1);

        assertThat(mounts.get(0).type()).isEqualTo("bind");
        assertThat(mounts.get(0).source()).isEqualTo("/path/in/host");
        assertThat(mounts.get(0).target()).isEqualTo("/path/in/container");
    }

    @Test
    void shouldSkipEmptyLine() {
        final DockerMounts dockerMounts = DockerMounts.fromString("type=volume, source=namedVolume, target=/path/in/container\n\ntype=bind, source=/path/in/host, target=/path/in/container2");

        assertThat(dockerMounts).isNotNull();
        assertThat(dockerMounts).hasSize(2);

        assertThat(dockerMounts.get(0).type()).isEqualTo("volume");
        assertThat(dockerMounts.get(1).type()).isEqualTo("bind");
    }

    @Test
    void shouldBuildMountFromDockerMount() {
        final DockerMounts dockerMounts = DockerMounts.fromString("source=namedVolume, target=/path/in/container\ntype=bind, src=/path/in/host, target=/path/in/container2, readonly");
        final Volume volume = mock(Volume.class);

        when(volume.name()).thenReturn("namedVolume");

        final List<Mount> mounts = dockerMounts.toMount();

        assertThat(mounts).hasSize(2);
        assertThat(mounts.get(0).type()).isEqualTo("volume");
        assertThat(mounts.get(0).source()).isEqualTo("namedVolume");
        assertThat(mounts.get(0).target()).isEqualTo("/path/in/container");
        assertThat(mounts.get(0).readOnly()).isEqualTo(false);

        assertThat(mounts.get(1).type()).isEqualTo("bind");
        assertThat(mounts.get(1).source()).isEqualTo("/path/in/host");
        assertThat(mounts.get(1).target()).isEqualTo("/path/in/container2");
        assertThat(mounts.get(1).readOnly()).isEqualTo(true);
    }
}
