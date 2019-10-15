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

package cd.go.contrib.elasticagents.dockerswarm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HostsTest {

    @Test
    void shouldReturnEmptyListWhenHostConfigIsNotProvided() {
        assertThat(new Hosts().hosts(null)).hasSize(0);
        assertThat(new Hosts().hosts("")).hasSize(0);
    }

    @Test
    void shouldReturnHostMappingForOneIpToOneHostnameMapping() {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host");
        assertThat(hosts).contains("10.0.0.1 foo-host");
    }

    @Test
    void shouldReturnHostMappingForOneIpToMAnyHostnameMapping() {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host bar-host");
        assertThat(hosts).contains("10.0.0.1 foo-host");
        assertThat(hosts).contains("10.0.0.1 bar-host");
    }

    @Test
    void shouldErrorOutIfHostEntryIsInvalid() {
        final List<String> errors = new Hosts().validate("foo-host 10.0.0.1");

        assertThat(errors).contains("'foo-host' is not an IP string literal.");
    }

    @Test
    void shouldErrorOutIfHostEntryIsNotContainsHostName() {
        final List<String> errors = new Hosts().validate("10.0.0.1");

        assertThat(errors).contains("Host entry `10.0.0.1` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format.");
    }
}
