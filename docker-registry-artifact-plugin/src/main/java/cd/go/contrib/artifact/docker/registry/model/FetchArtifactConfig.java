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
import cd.go.contrib.artifact.docker.registry.annotation.Validatable;
import cd.go.contrib.artifact.docker.registry.utils.Util;
import cd.go.plugin.base.validation.ValidationResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;

public class FetchArtifactConfig implements Validatable {
    @Expose
    @SerializedName("EnvironmentVariablePrefix")
    @FieldMetadata(key = "EnvironmentVariablePrefix", required = false)
    private String environmentVariablePrefix;

    @Expose
    @SerializedName("SkipImagePulling")
    @FieldMetadata(key = "SkipImagePulling")
    private boolean skipImagePulling;

    public FetchArtifactConfig() {
    }

    public FetchArtifactConfig(String environmentVariablePrefix, String skipImagePulling) {
        this.environmentVariablePrefix = environmentVariablePrefix;
        this.skipImagePulling = Boolean.parseBoolean(skipImagePulling);
    }

    public static FetchArtifactConfig fromJSON(String json) {
        return Util.GSON.fromJson(json, FetchArtifactConfig.class);
    }

    public String getEnvironmentVariablePrefix() {
        return environmentVariablePrefix;
    }

    public boolean getSkipImagePulling() {
        return skipImagePulling;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = new ValidationResult();
        if (StringUtils.isNotBlank(environmentVariablePrefix) && !environmentVariablePrefix.matches("(?i)[a-z][a-z0-9_]*")) {
            validationResult.add("EnvironmentVariablePrefix", "Invalid environment name prefix. Valid prefixes contain characters, numbers, and underscore; and can't start with a number.");
        }
        return validationResult;
    }
}
