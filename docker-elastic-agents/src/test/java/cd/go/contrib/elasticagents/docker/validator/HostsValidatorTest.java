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

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration.HOSTS;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class HostsValidatorTest {

    private HostsValidator validator;

    @BeforeEach
    void setUp() {
        validator = new HostsValidator();
    }

    @Test
    void shouldReturnEmptyListWhenHostConfigIsNotProvided() {
        assertThat(validator.validate(singletonMap(HOSTS, null))).hasSize(0);
        assertThat(new HostsValidator().validate(Map.of(HOSTS, ""))).hasSize(0);
    }

    @Test
    void shouldAllowOneIPToOneHostnameMapping() {
        final ValidationResult result = validator.validate(Map.of(HOSTS, "10.0.0.1 foo-host"));

        assertThat(result).hasSize(0);
    }

    @Test
    void shouldAllowOneIPToManyHostnameMapping() {
        final ValidationResult result = validator.validate(Map.of(HOSTS, "10.0.0.1 foo-host bar-host"));

        assertThat(result).hasSize(0);
    }

    @Test
    void shouldIgnoreEmptyLines() {
        final ValidationResult result = validator.validate(Map.of(HOSTS, "10.0.0.1 foo-host\n\n\n 10.0.0.2 bar-host"));

        assertThat(result).hasSize(0);
    }

    @Test
    void shouldValidateIPAddress() {
        ValidationResult result = validator.validate(Map.of(HOSTS, "10.0.0.foo hostname"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("'10.0.0.foo' is not an IP string literal.");
    }

    @Test
    void shouldValidateInvalidHostConfig() {
        ValidationResult result = validator.validate(Map.of(HOSTS, "some-config"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Host entry `some-config` is invalid.");
    }
}
