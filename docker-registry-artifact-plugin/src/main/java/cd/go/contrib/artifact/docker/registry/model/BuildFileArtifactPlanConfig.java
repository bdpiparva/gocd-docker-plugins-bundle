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

import cd.go.contrib.artifact.docker.registry.annotation.FieldMetadata;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class BuildFileArtifactPlanConfig extends ArtifactPlanConfig {
    @Expose
    @SerializedName("BuildFile")
    @FieldMetadata(key = "BuildFile")
    private String buildFile;

    public BuildFileArtifactPlanConfig(String buildFile) {
        this.buildFile = buildFile;
    }

    public String getBuildFile() {
        return buildFile;
    }


    @Override
    public DockerImage imageToPush(String agentWorkingDirectory, Map<String, String> environmentVariables) {
        try {
            return DockerImage.fromFile(new File(agentWorkingDirectory, getBuildFile()));
        } catch (JsonSyntaxException e) {
            throw new RuntimeException(String.format("File[%s] content is not a valid json. It must contain json data `{'image':'DOCKER-IMAGE-NAME', 'tag':'TAG'}` format.", buildFile));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildFileArtifactPlanConfig that = (BuildFileArtifactPlanConfig) o;
        return Objects.equals(buildFile, that.buildFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildFile);
    }
}
