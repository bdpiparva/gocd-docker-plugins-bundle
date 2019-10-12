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

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class HostsTest {

    @Test
    public void shouldReturnEmptyListWhenHostConfigIsNotProvided() throws Exception {
        assertThat(new Hosts().hosts(null), hasSize(0));
        assertThat(new Hosts().hosts(""), hasSize(0));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToOneHostnameMapping() throws Exception {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host");
        assertThat(hosts, hasItem("10.0.0.1 foo-host"));
    }

    @Test
    public void shouldReturnHostMappingForOneIpToMAnyHostnameMapping() throws Exception {
        final List<String> hosts = new Hosts().hosts("10.0.0.1 foo-host bar-host");
        assertThat(hosts, hasItem("10.0.0.1 foo-host"));
        assertThat(hosts, hasItem("10.0.0.1 bar-host"));
    }

    @Test
    public void shouldErrorOutIfHostEntryIsInvalid() throws Exception {
        final List<String> errors = new Hosts().validate("foo-host 10.0.0.1");

        assertThat(errors, contains("'foo-host' is not an IP string literal."));
    }

    @Test
    public void shouldErrorOutIfHostEntryIsNotContainsHostName() throws Exception {
        final List<String> errors = new Hosts().validate("10.0.0.1");

        assertThat(errors, contains("Host entry `10.0.0.1` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format."));
    }
}
