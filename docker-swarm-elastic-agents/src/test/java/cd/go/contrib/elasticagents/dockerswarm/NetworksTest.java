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

import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NetworksTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldReturnEmptyListWhenNetworkConfigIsNotProvided() throws Exception {
        assertThat(Networks.fromString(null, Collections.emptyList()), hasSize(0));
        assertThat(Networks.fromString("", Collections.emptyList()), hasSize(0));
    }

    @Test
    public void shouldReturnNetworkAttachmentConfigListFromString() throws Exception {
        final Network swarmNetwork = mock(Network.class);
        when(swarmNetwork.name()).thenReturn("frontend");

        final List<NetworkAttachmentConfig> serviceNetworks = Networks.fromString("frontend", asList(swarmNetwork));

        assertNotNull(serviceNetworks);
        assertThat(serviceNetworks, hasSize(1));

        assertThat(serviceNetworks.get(0).target(), is("frontend"));
    }

    @Test
    public void shouldErrorOutWhenNetworkDoesNotExist() throws Exception {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Network with name `frontend` does not exist.");

        final List<NetworkAttachmentConfig> serviceNetworks = Networks.fromString("frontend", Collections.emptyList());
    }
}
