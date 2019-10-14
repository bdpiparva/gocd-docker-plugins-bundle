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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ImageTagArtifactPlanConfig extends ArtifactPlanConfig {

    @Expose
    @SerializedName("Image")
    @FieldMetadata(key = "Image")
    private String image;

    @Expose
    @SerializedName("Tag")
    @FieldMetadata(key = "Tag")
    private String tag;

    public ImageTagArtifactPlanConfig(String image, Optional<String> tag) {
        this.image = image;
        this.tag = tag.orElse("latest");
    }

    @Override
    public DockerImage imageToPush(String agentWorkingDirectory,
                                   Map<String, String> environmentVariables) throws UnresolvedPropertyException {
        String evaluatedTag = evaluate(tag, environmentVariables, "tag");
        String evaluatedImage = evaluate(image, environmentVariables, "image");
        return new DockerImage(evaluatedImage, evaluatedTag);
    }

    public String getImage() {
        return image;
    }

    public String getTag() {
        return tag;
    }

    private String evaluate(String property,
                            Map<String, String> environmentVariables,
                            String propertyName) throws UnresolvedPropertyException {
        return new EnvironmentVariableResolver(property, propertyName).resolve(environmentVariables);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageTagArtifactPlanConfig that = (ImageTagArtifactPlanConfig) o;
        return Objects.equals(image, that.image) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, tag);
    }
}
