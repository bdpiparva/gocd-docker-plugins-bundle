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

import java.util.Map;

import static cd.go.contrib.elasticagents.common.validators.GoServerURLValidator.GO_SERVER_URL;
import static org.assertj.core.api.Assertions.assertThat;

class GoServerURLValidatorTest {
    private final GoServerURLValidator goServerURLMetadata = new GoServerURLValidator();

    @Test
    void shouldCheckIfStringIsValidUrl() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "foobar"));

        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must be a valid URL (https://example.com:8154/go)");
    }

    @Test
    void shouldCheckIfSchemeIsValid() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "example.com"));

        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must be a valid URL (https://example.com:8154/go)");
    }

    @Test
    void shouldCheckIfSchemeIsHTTPS() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "http://example.com"));

        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must be a valid HTTPs URL (https://example.com:8154/go)");
    }

    @Test
    void shouldCheckForLocalhost() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://localhost:8154/go"));

        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must not be localhost, since this gets resolved on the agents");

        result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://127.0.0.1:8154/go"));

        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must not be localhost, since this gets resolved on the agents");
    }

    @Test
    void shouldCheckIfUrlEndsWithContextGo() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://example.com:8154/"));
        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must be a valid URL ending with '/go' (https://example.com:8154/go)");

        result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://example.com:8154/crimemastergogo"));
        assertThat(result.get(0).getMessage()).isEqualTo("Go Server URL must be a valid URL ending with '/go' (https://example.com:8154/go)");
    }

    @Test
    void shouldReturnNullForValidUrls() {
        ValidationResult result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://example.com:8154/go"));
        assertThat(result).isEmpty();

        result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://example.com:8154/go/"));
        assertThat(result).isEmpty();

        result = goServerURLMetadata.validate(Map.of(GO_SERVER_URL, "https://example.com:8154/foo/go/"));
        assertThat(result).isEmpty();
    }
}
