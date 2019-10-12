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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import cd.go.contrib.elasticagents.dockerswarm.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.model.ValidationError;
import cd.go.contrib.elasticagents.dockerswarm.model.ValidationResult;
import cd.go.contrib.elasticagents.dockerswarm.requests.ClusterProfileValidateRequest;
import cd.go.contrib.elasticagents.dockerswarm.validator.PrivateDockerRegistrySettingsValidator;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

public class ClusterProfileValidateRequestExecutor implements RequestExecutor {
    private ClusterProfileValidateRequest request;

    public ClusterProfileValidateRequestExecutor(ClusterProfileValidateRequest request) {
        this.request = request;
    }

    public GoPluginApiResponse execute() {
        final List<String> knownFields = new ArrayList<>();
        final ValidationResult validationResult = new ValidationResult();

        for (Metadata field : GetClusterProfileMetadataExecutor.FIELDS) {
            knownFields.add(field.getKey());
            validationResult.addError(field.validate(request.getProperties().get(field.getKey())));
        }
        final Set<String> set = new HashSet<>(request.getProperties().keySet());
        set.removeAll(knownFields);

        if (!set.isEmpty()) {
            for (String key : set) {
                validationResult.addError(key, "Is an unknown property.");
            }
        }
        List<Map<String, String>> validateErrors = new PrivateDockerRegistrySettingsValidator().validate(request);
        validateErrors.forEach(error -> validationResult.addError(new ValidationError(error.get("key"), error.get("message"))));
        return DefaultGoPluginApiResponse.success(validationResult.toJSON());
    }

}
