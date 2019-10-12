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

package cd.go.contrib.elasticagents.docker.validator;

import cd.go.contrib.elasticagents.docker.models.Hosts;
import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;

import java.util.List;
import java.util.Map;

import static cd.go.contrib.elasticagents.docker.models.ElasticProfileConfiguration.HOSTS;

public class HostsValidator implements Validator {
    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        List<String> errors = new Hosts(requestBody.get(HOSTS)).getErrors();
        if (errors.isEmpty()) {
            return new ValidationResult();
        }

        ValidationResult result = new ValidationResult();
        result.add(HOSTS, String.join("\n", errors));
        return result;
    }
}
