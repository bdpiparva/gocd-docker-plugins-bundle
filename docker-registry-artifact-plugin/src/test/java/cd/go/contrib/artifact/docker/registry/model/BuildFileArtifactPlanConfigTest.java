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

package cd.go.contrib.artifact.docker.registry.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.MockitoAnnotations.initMocks;

class BuildFileArtifactPlanConfigTest {
    private File agentWorkingDir;
    private final Map<String, String> environmentVariables = new HashMap<>();

    @BeforeEach
    void setUp(@TempDir File tempDir) {
        initMocks(this);
        agentWorkingDir = tempDir;
    }

    @Test
    void shouldReadImageAndTagBuildFile() throws IOException, UnresolvedPropertyException {
        Path file = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(file, "{\"image\":\"alpine\",\"tag\":\"3.6\"}".getBytes());

        final ArtifactPlanConfig artifactPlanConfig = new BuildFileArtifactPlanConfig("build-file.json");
        final DockerImage dockerImage = artifactPlanConfig.imageToPush(agentWorkingDir.getAbsolutePath(), environmentVariables);

        assertThat(dockerImage.getImage()).isEqualTo("alpine");
        assertThat(dockerImage.getTag()).isEqualTo("3.6");
    }

    @Test
    void shouldErrorOutWhenFileContentIsNotAValidJSON() throws IOException {
        Path file = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(file, "bar".getBytes());
        final ArtifactPlanConfig artifactPlanConfig = new BuildFileArtifactPlanConfig("build-file.json");

        assertThatCode(() -> artifactPlanConfig.imageToPush(agentWorkingDir.getAbsolutePath(), environmentVariables))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File[build-file.json] content is not a valid json. It must contain json data `{'image':'DOCKER-IMAGE-NAME', 'tag':'TAG'}` format.");
    }

    @Test
    void shouldErrorOutWhenFileContentIsJSONArray() throws IOException {
        Path file = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(file, "[{}]".getBytes());
        final ArtifactPlanConfig artifactPlanConfig = new BuildFileArtifactPlanConfig("build-file.json");

        assertThatCode(() -> artifactPlanConfig.imageToPush(agentWorkingDir.getAbsolutePath(), environmentVariables))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("File[build-file.json] content is not a valid json. It must contain json data `{'image':'DOCKER-IMAGE-NAME', 'tag':'TAG'}` format.");
    }

    @Test
    void shouldErrorOutWhenFileDoesNotExist() {
        final ArtifactPlanConfig artifactPlanConfig = new BuildFileArtifactPlanConfig("random.json");

        assertThatCode(() -> artifactPlanConfig.imageToPush(agentWorkingDir.getAbsolutePath(), environmentVariables))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(format("%s/random.json (No such file or directory)", agentWorkingDir.getAbsolutePath()));
    }
}
