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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.MockitoAnnotations.initMocks;

class DockerImageTest {
    private File agentWorkingDir;

    @BeforeEach
    void setUp(@TempDir File tempDir) {
        initMocks(this);
        agentWorkingDir = tempDir;
    }

    @Test
    void shouldDeserializeFileToDockerImage() throws IOException {
        Path path = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(path, "{\"image\":\"alpine\",\"tag\":\"3.6\"}".getBytes());

        final DockerImage dockerImage = DockerImage.fromFile(path.toFile());

        assertThat(dockerImage.getImage()).isEqualTo("alpine");
        assertThat(dockerImage.getTag()).isEqualTo("3.6");
    }

    @Test
    void shouldErrorOutWhenFileContentIsNotAValidJSON() throws IOException {
        Path path = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(path, "bar".getBytes());

        assertThatCode(() -> DockerImage.fromFile(path.toFile()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected BEGIN_OBJECT but was STRING at line 1 column 1 path");
    }

    @Test
    void shouldErrorOutWhenFileContentIsJSONArray() throws IOException {
        Path path = Paths.get(agentWorkingDir.getAbsolutePath(), "build-file.json");
        Files.write(path, "[{}]".getBytes());

        assertThatCode(() -> DockerImage.fromFile(path.toFile()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected BEGIN_OBJECT but was BEGIN_ARRAY at line 1 column 2 path");
    }

    @Test
    void shouldErrorOutWhenFileDoesNotExist() {
        assertThatCode(() -> DockerImage.fromFile(new File(agentWorkingDir, "random.json")))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining(String.format("%s/random.json (No such file or directory)", agentWorkingDir.getAbsolutePath()));
    }
}
