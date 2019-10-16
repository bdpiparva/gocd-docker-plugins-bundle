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

package cd.go.contrib.elasticagents.common.validators;

import cd.go.plugin.base.validation.ValidationResult;
import cd.go.plugin.base.validation.Validator;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;

public class GoServerURLValidator implements Validator {
    public static final String GO_SERVER_URL = "go_server_url";

    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        ValidationResult result = new ValidationResult();
        String url = requestBody.get(GO_SERVER_URL);
        if (isBlank(url)) {
            //Default validation will take care of blank check based on metadata
            return result;
        }

        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(url);
        } catch (URISyntaxException e) {
            return addError(result, "%s must be a valid URL (https://example.com:8154/go)");
        }

        if (isBlank(uriBuilder.getScheme())) {
            return addError(result, "%s must be a valid URL (https://example.com:8154/go)");
        }

        if (!uriBuilder.getScheme().equalsIgnoreCase("https")) {
            return addError(result, "%s must be a valid HTTPs URL (https://example.com:8154/go)");
        }

        if (uriBuilder.getHost().equalsIgnoreCase("localhost") || uriBuilder.getHost().equalsIgnoreCase("127.0.0.1")) {
            return addError(result, "%s must not be localhost, since this gets resolved on the agents");
        }

        if (!(uriBuilder.getPath().endsWith("/go") || uriBuilder.getPath().endsWith("/go/"))) {
            return addError(result, "%s must be a valid URL ending with '/go' (https://example.com:8154/go)");
        }

        return result;
    }

    private ValidationResult addError(ValidationResult result, String message) {
        result.add(GO_SERVER_URL, format(message, "Go Server URL"));
        return result;
    }
}
