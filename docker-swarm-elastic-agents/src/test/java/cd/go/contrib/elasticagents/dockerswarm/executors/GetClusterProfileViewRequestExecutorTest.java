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

package cd.go.contrib.elasticagents.dockerswarm.executors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static cd.go.plugin.base.ResourceReader.readResource;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GetClusterProfileViewRequestExecutorTest {

    @Test
    public void shouldRenderTheTemplateInJSON() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), type);
        assertThat(hashSet, hasEntry("template", readResource("/plugin-settings.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() throws Exception {
        String template = readResource("/plugin-settings.template.html");

        for (Metadata field : GetClusterProfileMetadataExecutor.FIELDS) {
            assertThat(template, containsString("ng-model=\"" + field.getKey() + "\""));
            assertThat(template, containsString("<span class=\"form_error\" ng-show=\"GOINPUTNAME[" + field.getKey() +
                    "].$error.server\">{{GOINPUTNAME[" + field.getKey() +
                    "].$error.server}}</span>"));
        }
    }
}
