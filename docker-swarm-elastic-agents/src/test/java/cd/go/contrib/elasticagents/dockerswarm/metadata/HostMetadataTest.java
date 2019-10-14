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
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class HostMetadataTest {

    @Test
    public void shouldValidateHostConfig() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new HostMetadata("Hosts", false, false).validate("10.0.0.1 hostname", validationResult);

        assertTrue(validationResult.isEmpty());

        new HostMetadata("Hosts", false, false)
                .validate("some-config", validationResult);

        assertFalse(validationResult.isEmpty());
        assertThat(validationResult.find("Hosts").get().getMessage(), is("Host entry `some-config` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format."));
    }

    @Test
    public void shouldValidateHostConfigWhenRequireField() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new HostMetadata("Hosts", true, false).validate(null, validationResult);

        assertFalse(validationResult.isEmpty());
        assertThat(validationResult.find("Hosts").get().getMessage(), is("Hosts must not be blank."));
    }
}
