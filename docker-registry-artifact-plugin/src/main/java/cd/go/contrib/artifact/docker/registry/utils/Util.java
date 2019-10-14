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

package cd.go.contrib.artifact.docker.registry.utils;

import cd.go.contrib.artifact.docker.registry.annotation.FieldMetadata;
import cd.go.contrib.artifact.docker.registry.annotation.FieldMetadataTypeAdapter;
import cd.go.contrib.artifact.docker.registry.model.ArtifactPlanConfig;
import cd.go.contrib.artifact.docker.registry.model.ArtifactPlanConfigTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .registerTypeAdapter(ArtifactPlanConfig.class, new ArtifactPlanConfigTypeAdapter())
            .registerTypeAdapter(FieldMetadata.class, new FieldMetadataTypeAdapter())
            .create();

}

