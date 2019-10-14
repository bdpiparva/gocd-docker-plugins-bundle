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

package cd.go.contrib.elasticagents.dockerswarm.requests;

import cd.go.contrib.elasticagents.dockerswarm.RequestExecutor;
import cd.go.contrib.elasticagents.dockerswarm.executors.ClusterProfileValidateRequestExecutor;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ClusterProfileValidateRequest extends HashMap<String, String> {
    public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private Map<String, String> properties;

    public ClusterProfileValidateRequest(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public static ClusterProfileValidateRequest fromJSON(String json) {
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        final Map<String, String> properties = GSON.fromJson(json, type);
        return new ClusterProfileValidateRequest(properties);
    }

    public RequestExecutor executor() {
        return new ClusterProfileValidateRequestExecutor(this);
    }
}
