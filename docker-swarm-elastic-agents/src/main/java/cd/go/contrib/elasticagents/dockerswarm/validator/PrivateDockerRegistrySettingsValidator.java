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

import cd.go.contrib.elasticagents.dockerswarm.executors.GetClusterProfileMetadataExecutor;
import cd.go.contrib.elasticagents.dockerswarm.executors.Metadata;
import cd.go.contrib.elasticagents.dockerswarm.requests.ClusterProfileValidateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class PrivateDockerRegistrySettingsValidator {

    public List<Map<String, String>> validate(ClusterProfileValidateRequest request) {
        final List<Map<String, String>> result = new ArrayList<>();
        final boolean useDockerAuthInfo = Boolean.valueOf(request.getProperties().get(GetClusterProfileMetadataExecutor.ENABLE_PRIVATE_REGISTRY_AUTHENTICATION.getKey()));
        if (!useDockerAuthInfo) {
            return result;
        }

        validate(GetClusterProfileMetadataExecutor.PRIVATE_REGISTRY_SERVER, request, result);
        validate(GetClusterProfileMetadataExecutor.PRIVATE_REGISTRY_USERNAME, request, result);
        validate(GetClusterProfileMetadataExecutor.PRIVATE_REGISTRY_PASSWORD, request, result);
        return result;
    }

    private void validate(Metadata field, ClusterProfileValidateRequest request, List<Map<String, String>> errorResult) {
        if (isBlank(request.getProperties().get(field.getKey()))) {
            Map<String, String> result = new HashMap<>();
            result.put("key", field.getKey());
            result.put("message", field.getKey()+ " must not be blank.");
            errorResult.add(result);
        }
    }
}
