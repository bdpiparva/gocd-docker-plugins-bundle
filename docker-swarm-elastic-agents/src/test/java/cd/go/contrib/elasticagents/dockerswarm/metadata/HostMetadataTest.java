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

package cd.go.contrib.elasticagents.dockerswarm.metadata;

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HostMetadataTest {
    @Test
    void shouldValidateHostConfig() {
        ValidationResult validationResult = new ValidationResult();
        new HostMetadata("Hosts", false, false).validate("10.0.0.1 hostname", validationResult);

        assertThat(validationResult.isEmpty()).isTrue();

        new HostMetadata("Hosts", false, false)
                .validate("some-config", validationResult);

        assertThat(validationResult.isEmpty()).isFalse();
        assertThat(validationResult.find("Hosts").get().getMessage()).isEqualTo("Host entry `some-config` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format.");
    }

    @Test
    void shouldValidateHostConfigWhenRequireField() {
        ValidationResult validationResult = new ValidationResult();
        new HostMetadata("Hosts", true, false).validate(null, validationResult);

        assertThat(validationResult.isEmpty()).isFalse();
        assertThat(validationResult.find("Hosts").get().getMessage()).isEqualTo("Hosts must not be blank.");
    }
}
