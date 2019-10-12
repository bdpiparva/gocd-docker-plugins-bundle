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

import cd.go.contrib.elasticagents.dockerswarm.ClusterProfileProperties;
import cd.go.contrib.elasticagents.dockerswarm.DockerServices;
import cd.go.contrib.elasticagents.dockerswarm.executors.ClusterStatusReportExecutor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class ClusterStatusReportRequest {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Expose
    @SerializedName("cluster_profile_properties")
    private ClusterProfileProperties clusterProfile;

    public ClusterStatusReportRequest() {
    }

    public ClusterStatusReportRequest(Map<String, String> clusterProfileConfigurations) {
        this.clusterProfile = ClusterProfileProperties.fromConfiguration(clusterProfileConfigurations);
    }

    public ClusterProfileProperties getClusterProfile() {
        return clusterProfile;
    }

    public static ClusterStatusReportRequest fromJSON(String json) {
        return GSON.fromJson(json, ClusterStatusReportRequest.class);
    }

    public ClusterStatusReportExecutor executor(DockerServices dockerServices) throws IOException {
        return new ClusterStatusReportExecutor(this, dockerServices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterStatusReportRequest that = (ClusterStatusReportRequest) o;
        return Objects.equals(clusterProfile, that.clusterProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterProfile);
    }

    @Override
    public String toString() {
        return "ClusterStatusReportRequest{" +
                "clusterProfile=" + clusterProfile +
                '}';
    }

}
