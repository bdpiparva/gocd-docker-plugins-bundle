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

package cd.go.contrib.elasticagents.common.requests;

import cd.go.contrib.elasticagents.common.models.ClusterProfileConfiguration;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import static cd.go.plugin.base.GsonTransformer.fromJson;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode
public abstract class AbstractClusterStatusReportRequest<C extends ClusterProfileConfiguration> {
    @Expose
    @SerializedName("cluster_profile_properties")
    private C clusterProfileConfiguration;

    public AbstractClusterStatusReportRequest() {
    }

    public static <T extends AbstractClusterStatusReportRequest> T fromJSON(String json,
                                                                            Class<T> type) {
        return fromJson(json, type);
    }
}
