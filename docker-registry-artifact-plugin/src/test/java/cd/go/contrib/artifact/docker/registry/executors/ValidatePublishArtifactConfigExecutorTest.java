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

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class ValidatePublishArtifactConfigExecutorTest {
    @Mock
    private GoPluginApiRequest request;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void shouldValidateRequestWithBuildFile() throws Exception {
        String requestBody = new JSONObject().put("BuildFile", "").toString();
        when(request.requestBody()).thenReturn(requestBody);

        final GoPluginApiResponse response = new ValidatePublishArtifactConfigExecutor(request).execute();

        String expectedJSON = "[" +
                "  {" +
                "    'key': 'BuildFile'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }," +
                "  {" +
                "    'key': 'Image'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }" +
                "]";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldValidateRequestWithImageAndTag() throws JSONException {
        String requestBody = new JSONObject()
                .put("Image", "")
                .put("Tag", "")
                .toString();
        when(request.requestBody()).thenReturn(requestBody);

        final GoPluginApiResponse response = new ValidatePublishArtifactConfigExecutor(request).execute();

        String expectedJSON = "[" +
                "  {" +
                "    'key': 'BuildFile'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }," +
                "  {" +
                "    'key': 'Image'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }" +
                "]";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldValidateInvalidRequest() throws JSONException {
        when(request.requestBody()).thenReturn("{}");

        final GoPluginApiResponse response = new ValidatePublishArtifactConfigExecutor(request).execute();

        String expectedJSON = "[" +
                "  {" +
                "    'key': 'BuildFile'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }," +
                "  {" +
                "    'key': 'Image'," +
                "    'message': 'Either `Image` or `BuildFile` should be specified.'" +
                "  }" +
                "]";
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldValidateRequestContainingAllFields() throws JSONException {
        String requestBody = new JSONObject()
                .put("BuildFile", "build.json")
                .put("Image", "alpine")
                .put("Tag", "latest")
                .toString();
        when(request.requestBody()).thenReturn(requestBody);

        GoPluginApiResponse response = new ValidatePublishArtifactConfigExecutor(request).execute();

        String expectedResponse = new JSONArray().put(
                new JSONObject()
                        .put("key", "Image")
                        .put("message", "Either `Image` or `BuildFile` should be specified.")
        ).put(new JSONObject()
                .put("key", "BuildFile")
                .put("message", "Either `Image` or `BuildFile` should be specified.")).toString();

        JSONAssert.assertEquals(expectedResponse, response.responseBody(), true);
    }
}
