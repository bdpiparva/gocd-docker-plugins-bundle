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

import com.google.gson.JsonParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


class ArtifactPlanConfigTypeAdapterTest {

    @Test
    void shouldAllowBlankTagAndDefaultToLatest() throws JSONException {
        List<String> inputs = Arrays.asList(
                new JSONObject().put("Image", "alpine").toString(),
                new JSONObject().put("Image", "alpine").put("Tag", "").toString(),
                new JSONObject().put("Image", "alpine").put("Tag", (String) null).toString(),
                new JSONObject().put("Image", "alpine").toString());

        for (String json : inputs) {
            ArtifactPlanConfig artifactPlanConfig = ArtifactPlanConfig.fromJSON(json);

            assertThat(artifactPlanConfig).isInstanceOf(ImageTagArtifactPlanConfig.class);
            assertThat(((ImageTagArtifactPlanConfig) artifactPlanConfig).getImage()).isEqualTo("alpine");
            assertThat(((ImageTagArtifactPlanConfig) artifactPlanConfig).getTag()).isEqualTo("latest");
        }
    }

    @Test
    void shouldDeserializeToBuildFilePlanConfig() throws JSONException {
        List<String> inputs = Arrays.asList(
                new JSONObject().put("BuildFile", "info.json").put("Tag", "").put("Image", "").toString(),
                new JSONObject().put("BuildFile", "info.json").toString(),
                new JSONObject().put("BuildFile", "info.json").put("Image", (String) null).toString());

        for (String json : inputs) {
            ArtifactPlanConfig artifactPlanConfig = ArtifactPlanConfig.fromJSON(json);

            assertThat(artifactPlanConfig).isInstanceOf(BuildFileArtifactPlanConfig.class);
            assertThat(((BuildFileArtifactPlanConfig) artifactPlanConfig).getBuildFile()).isEqualTo("info.json");
        }
    }

    @Test
    void shouldThrowAnExceptionWhenBothBuildFileAndImageAreProvided() throws JSONException {
        List<String> inputs = Collections.singletonList(
                new JSONObject().put("BuildFile", "info.json").put("Tag", "").put("Image", "fml").toString());

        for (String json : inputs) {
            try {
                ArtifactPlanConfig artifactPlanConfig = ArtifactPlanConfig.fromJSON(json);
                fail("Should not reach here");
            } catch (JsonParseException e) {
                assertThat(e.getMessage()).isEqualTo("Ambiguous or unknown json. Either `Image` or`BuildFile` property must be specified.");
            }
        }
    }


    @Test
    void shouldParseConfigurationsWithJsonNull() throws JSONException {
        String json = "{\"BuildFile\": null, \"Image\": \"alpine\"}";
        ArtifactPlanConfig artifactPlanConfig = ArtifactPlanConfig.fromJSON(json);

        assertThat(artifactPlanConfig).isInstanceOf(ImageTagArtifactPlanConfig.class);
        assertThat(((ImageTagArtifactPlanConfig) artifactPlanConfig).getImage()).isEqualTo("alpine");
    }
}
