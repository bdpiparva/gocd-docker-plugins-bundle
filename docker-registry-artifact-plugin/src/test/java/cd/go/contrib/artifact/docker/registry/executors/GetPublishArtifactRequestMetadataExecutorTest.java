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

package cd.go.contrib.artifact.docker.registry.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

class GetPublishArtifactRequestMetadataExecutorTest {
    @Test
    void shouldReturnPublishArtifactMetadata() throws JSONException {
        final GoPluginApiResponse response = new GetPublishArtifactConfigMetadataExecutor().execute();

        final String expectedJSON = "[" +
                "{\"key\":\"BuildFile\",\"metadata\":{\"required\":false,\"secure\":false}}," +
                "{\"key\":\"Image\",\"metadata\":{\"required\":false,\"secure\":false}}," +
                "{\"key\":\"Tag\",\"metadata\":{\"required\":false,\"secure\":false}}" +
                "]";

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), true);
    }
}
