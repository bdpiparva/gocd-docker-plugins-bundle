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

import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;

import java.util.*;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Networks {
    public static List<NetworkAttachmentConfig> fromString(String networkConfig, List<Network> dockerNetworks) {
        if (isBlank(networkConfig)) {
            return Collections.emptyList();
        }

        final Map<String, Network> availableNetworks = dockerNetworks.stream().collect(Collectors.toMap(o -> o.name(), o -> o));

        final List<NetworkAttachmentConfig> serviceNetworks = new ArrayList<>();
        final Collection<String> networkEntries = Util.splitIntoLinesAndTrimSpaces(networkConfig);
        networkEntries.forEach(networkEntry -> {
            final Network availableNetwork = availableNetworks.get(networkEntry);

            if (availableNetwork == null) {
                throw new RuntimeException(format("Network with name `{0}` does not exist.", networkEntry));
            }

            DockerSwarmPlugin.LOG.debug(format("Using network `{0}` with id `{1}`.", networkEntry, availableNetwork.id()));
            serviceNetworks.add(NetworkAttachmentConfig.builder().target(networkEntry).build());
        });

        return serviceNetworks;
    }
}
