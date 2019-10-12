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

package cd.go.contrib.elasticagents.docker.validator;

import cd.go.contrib.elasticagents.docker.MemorySpecification;
import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;

import java.util.Map;

import static cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration.MAX_MEMORY;
import static cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration.RESERVED_MEMORY;

/**
 * Check that max memory is not lower than reserved memory.
 */
public class ContainerMemorySettingsValidator implements Validator {
    @Override
    public ValidationResult validate(Map<String, String> elasticProfile) {
        ValidationResult result = new ValidationResult();

        Long maxMemory = parse(elasticProfile.get(MAX_MEMORY), result);
        Long reservedMemory = parse(elasticProfile.get(RESERVED_MEMORY), result);

        if (result.isEmpty()) {
            if (maxMemory != null && maxMemory < 4 * 1024 * 1024) {
                // this is docker limitation,
                // see https://docs.docker.com/config/containers/resource_constraints/#memory
                result.add(MAX_MEMORY, "Minimum allowed value is 4M");
            } else if (maxMemory != null && reservedMemory != null && maxMemory < reservedMemory) {
                result.add(MAX_MEMORY, "Max memory is lower than reserved memory");
            }
        }

        return result;
    }

    private Long parse(String maxMemoryInput, ValidationResult result) {
        try {
            return MemorySpecification.parse(maxMemoryInput);
        } catch (IllegalArgumentException e) {
            result.add(MAX_MEMORY, e.getMessage());
            return null;
        }
    }
}
