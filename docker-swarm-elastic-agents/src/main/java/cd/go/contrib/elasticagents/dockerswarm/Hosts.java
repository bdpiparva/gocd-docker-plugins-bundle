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
import com.google.common.net.InetAddresses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class Hosts {

    public List<String> hosts(String hostConfig) {
        if (isBlank(hostConfig)) {
            return Collections.emptyList();
        }

        List<String> hostMappings = new ArrayList<>();
        Collection<String> hostEntries = Util.splitIntoLinesAndTrimSpaces(hostConfig);

        hostEntries.forEach(hostEntry -> {
            hostMappings.addAll(toHosts(hostEntry));
        });

        return hostMappings;
    }

    private List<String> toHosts(String hostEntry) {
        String[] parts = hostEntry.split("\\s+");

        final String ipAddress = parts[0];

        List<String> hosts = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            hosts.add(format("{0} {1}", ipAddress, parts[i]));
        }

        return hosts;
    }

    public List<String> validate(String hostConfig) {
        if (isBlank(hostConfig)) {
            return Collections.emptyList();
        }

        List<String> errors = new ArrayList<>();
        Collection<String> hostEntries = Util.splitIntoLinesAndTrimSpaces(hostConfig);

        hostEntries.forEach(hostEntry -> {
            String[] parts = hostEntry.split("\\s+");
            if (parts.length < 2) {
                errors.add(format("Host entry `{0}` is invalid. Must be in `IP-ADDRESS HOST-1 HOST-2...` format.", hostEntry));
            } else {
                validateIpAddress(errors, parts[0]);
            }
        });

        return errors;
    }

    private void validateIpAddress(List<String> errors, String part) {
        try {
            InetAddresses.forString(part);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }
    }
}
