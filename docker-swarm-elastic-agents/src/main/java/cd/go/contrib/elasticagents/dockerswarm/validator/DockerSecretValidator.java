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

import cd.go.contrib.elasticagents.dockerswarm.DockerClientFactory;
import cd.go.contrib.elasticagents.dockerswarm.DockerSecrets;
import cd.go.contrib.elasticagents.dockerswarm.requests.CreateAgentRequest;
import cd.go.plugin.base.validation.ValidationResult;
import com.spotify.docker.client.DockerClient;

import static cd.go.contrib.elasticagents.dockerswarm.utils.Util.dockerApiVersionAtLeast;

public class DockerSecretValidator implements Validatable<CreateAgentRequest> {
    private final DockerClientFactory dockerClientFactory;

    public DockerSecretValidator() {
        this(DockerClientFactory.instance());
    }

    DockerSecretValidator(DockerClientFactory dockerClientFactory) {
        this.dockerClientFactory = dockerClientFactory;
    }

    @Override
    public ValidationResult validate(CreateAgentRequest createAgentRequest) {
        final ValidationResult validationResult = new ValidationResult();
        try {
            final DockerSecrets dockerSecrets = DockerSecrets.fromString(createAgentRequest.getElasticProfileConfiguration().getSecrets());
            if (!dockerSecrets.isEmpty()) {
                DockerClient dockerClient = dockerClientFactory.docker(createAgentRequest.getClusterProfileProperties());
                if (!dockerApiVersionAtLeast(dockerClient, "1.26")) {
                    throw new RuntimeException("Docker secret requires api version 1.26 or higher.");
                }
                dockerSecrets.toSecretBind(dockerClient.listSecrets());
            }
        } catch (Exception e) {
            validationResult.add("Secrets", e.getMessage());
        }

        return validationResult;
    }
}
