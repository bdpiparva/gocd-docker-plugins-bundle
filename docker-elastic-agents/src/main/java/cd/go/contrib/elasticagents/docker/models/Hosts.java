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

package cd.go.contrib.elasticagents.docker.models;

import cd.go.contrib.elasticagents.docker.utils.Util;
import com.google.common.net.InetAddresses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

public class Hosts extends ArrayList<String> {
    private List<String> errors = new ArrayList<>();

    public Hosts(String hostConfig) {
        final Collection<String> hostEntries = Util.splitIntoLinesAndTrimSpaces(hostConfig);

        for (String hostEntry : hostEntries) {
            if (validate(hostEntry)) {
                String[] parts = hostEntry.split("\\s+", 2);
                add(trimToEmpty(parts[1]) + ":" + trimToEmpty(parts[0]));
            }
        }
    }

    private boolean validate(String hostEntry) {
        String[] parts = hostEntry.split("\\s+", 2);
        if (parts.length != 2) {
            this.errors.add(format("Host entry `{0}` is invalid.", hostEntry));
            return false;
        }

        if (validIPAddress(parts[0])) {
            return true;
        }

        return false;
    }

    private boolean validIPAddress(String ipAddress) {
        try {
            InetAddresses.forString(ipAddress);
            return true;
        } catch (Exception e) {
            this.errors.add(e.getMessage());
        }

        return false;
    }

    public List<String> getErrors() {
        return errors;
    }
}
