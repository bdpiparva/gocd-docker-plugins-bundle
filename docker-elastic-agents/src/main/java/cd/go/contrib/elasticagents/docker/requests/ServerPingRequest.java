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

package cd.go.contrib.elasticagents.docker.requests;

import cd.go.contrib.elasticagents.docker.models.ClusterProfileProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static cd.go.plugin.base.GsonTransformer.fromJson;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class ServerPingRequest {
    @Expose
    @SerializedName("all_cluster_profile_properties")
    private List<ClusterProfileProperties> allClusterProfileProperties = new ArrayList<>();

    public ServerPingRequest() {
    }


    public static ServerPingRequest fromJSON(String json) {
        return fromJson(json, ServerPingRequest.class);
    }
}
