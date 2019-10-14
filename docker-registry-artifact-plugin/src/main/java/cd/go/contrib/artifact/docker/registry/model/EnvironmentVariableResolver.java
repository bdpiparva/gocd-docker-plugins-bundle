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

package cd.go.contrib.artifact.docker.registry.model;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Map;
import java.util.regex.Pattern;

public class EnvironmentVariableResolver {

    private static final Pattern ENVIRONMENT_VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*)\\}");
    private final String property;
    private String propertyName;

    public EnvironmentVariableResolver(String property, String propertyName) {
        this.property = property;
        this.propertyName = propertyName;
    }

    public String resolve(Map<String, String> environmentVariables) throws UnresolvedPropertyException {
        String evaluatedProperty = StrSubstitutor.replace(property, environmentVariables);
        if (ENVIRONMENT_VARIABLE_PATTERN.matcher(evaluatedProperty).find()) {
            throw new UnresolvedPropertyException(evaluatedProperty, propertyName);
        }
        return evaluatedProperty;
    }
}
