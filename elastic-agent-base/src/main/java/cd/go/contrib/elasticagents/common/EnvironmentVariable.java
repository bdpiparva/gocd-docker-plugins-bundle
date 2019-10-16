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

package cd.go.contrib.elasticagents.common;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.repeat;

@EqualsAndHashCode(doNotUseGetters = true)
public class EnvironmentVariable {
    private final List<String> secureVariableList = List.of("GO_EA_AUTO_REGISTER_KEY");
    private final String name;
    private final String value;

    public EnvironmentVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return secureVariableList.contains(getName()) ? getMaskedValue() : value;
    }

    private String getMaskedValue() {
        if (StringUtils.isBlank(this.value)) {
            return this.value;
        }

        int maskLength = this.value.length() - 3;
        return repeat("*", maskLength) + this.value.substring(maskLength);
    }

    public static List<EnvironmentVariable> parse(List<String> envVars) {
        if (envVars == null || envVars.isEmpty()) {
            return Collections.emptyList();
        }

        return envVars.stream()
                .filter(Objects::nonNull)
                .map(EnvironmentVariable::toEnv)
                .collect(Collectors.toList());
    }

    private static EnvironmentVariable toEnv(String envAsString) {
        String[] parts = envAsString.split("=", 2);
        return new EnvironmentVariable(parts[0], parts.length < 2 ? null : parts[1]);
    }
}
