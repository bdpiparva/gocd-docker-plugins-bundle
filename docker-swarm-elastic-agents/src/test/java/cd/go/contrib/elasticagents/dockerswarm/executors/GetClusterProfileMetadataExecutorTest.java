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
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static cd.go.contrib.elasticagents.dockerswarm.executors.GetClusterProfileMetadataExecutor.FIELDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class GetClusterProfileMetadataExecutorTest {

    @Test
    public void shouldSerializeAllFields() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();
        List<Metadata> allFields = new Gson().fromJson(response.responseBody(), new TypeToken<List<Metadata>>() {
        }.getType());

        assertEquals(allFields.size(), FIELDS.size());
    }

    @Test
    public void assertJsonStructure() throws Exception {
        GoPluginApiResponse response = new GetClusterProfileMetadataExecutor().execute();

        assertThat(response.responseCode(), is(200));
        String expectedJSON = "[" +
                "{" +
                "   \"key\":\"go_server_url\"," +
                "   \"metadata\":{" +
                "       \"required\":true," +
                "       \"secure\":false" +
                "   }" +
                "}," +
                "{" +
                "   \"key\":\"environment_variables\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":false" +
                "   }" +
                "}," +
                "{" +
                "   \"key\":\"max_docker_containers\"," +
                "   \"metadata\":{" +
                "       \"required\":true," +
                "       \"secure\":false" +
                "   }" +
                "}," +
                "{" +
                "   \"key\":\"docker_uri\"," +
                "   \"metadata\":{" +
                "       \"required\":true," +
                "       \"secure\":false" +
                "   }" +
                "}," +
                "{" +
                "   \"key\":\"auto_register_timeout\"," +
                "   \"metadata\":{" +
                "       \"required\":true," +
                "       \"secure\":false" +
                "   }" +
                "},{" +
                "   \"key\":\"docker_ca_cert\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":true" +
                "   }" +
                "},{" +
                "   \"key\":\"docker_client_key\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":true" +
                "   }" +
                "},{" +
                "   \"key\":\"docker_client_cert\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":true" +
                "   }" +
                "},{" +
                "   \"key\":\"enable_private_registry_authentication\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":false" +
                "   }" +
                "},{" +
                "   \"key\":\"private_registry_server\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":false" +
                "   }" +
                "},{" +
                "   \"key\":\"private_registry_username\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":false" +
                "   }" +
                "},{" +
                "   \"key\":\"private_registry_password\"," +
                "   \"metadata\":{" +
                "       \"required\":false," +
                "       \"secure\":true" +
                "   }" +
                "}]";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
