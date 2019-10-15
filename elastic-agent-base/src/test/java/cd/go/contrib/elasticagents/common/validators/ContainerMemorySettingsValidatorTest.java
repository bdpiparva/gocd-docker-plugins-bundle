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

package cd.go.contrib.elasticagents.common.validators;

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static cd.go.contrib.elasticagents.common.validators.ContainerMemorySettingsValidator.MAX_MEMORY;
import static cd.go.contrib.elasticagents.common.validators.ContainerMemorySettingsValidator.RESERVED_MEMORY;
import static org.assertj.core.api.Assertions.assertThat;

class ContainerMemorySettingsValidatorTest {
    @Test
    void minimumMaxMemoryValue() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY, "3M");

        ValidationResult result = new ContainerMemorySettingsValidator().validate(elasticProfile);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasKey(MAX_MEMORY)).isTrue();
        assertThat(result.get(0).getMessage()).isEqualTo("Minimum allowed value is 4M");
    }

    @Test
    void bothMemorySettingsAreEmpty() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY, "");
        elasticProfile.put(RESERVED_MEMORY, "");

        ValidationResult result = new ContainerMemorySettingsValidator().validate(elasticProfile);

        assertThat(result).isEmpty();
    }

    @Test
    void maxMemorySetReservedMemoryEmpty() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY, "2G");
        elasticProfile.put(RESERVED_MEMORY, "");

        ValidationResult result = new ContainerMemorySettingsValidator().validate(elasticProfile);

        assertThat(result).isEmpty();
    }

    @Test
    void maxMemoryIsLowerThanReservedMemory() {
        Map<String, String> elasticProfile = new HashMap<>();
        elasticProfile.put(MAX_MEMORY, "1G");
        elasticProfile.put(RESERVED_MEMORY, "2G");

        ValidationResult result = new ContainerMemorySettingsValidator().validate(elasticProfile);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasKey(MAX_MEMORY)).isTrue();
        assertThat(result.get(0).getMessage()).isEqualTo("Max memory is lower than reserved memory");
    }
}
