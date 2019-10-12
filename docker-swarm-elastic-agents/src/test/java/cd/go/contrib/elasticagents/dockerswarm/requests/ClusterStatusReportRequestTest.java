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

import com.google.gson.JsonObject;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;

public class ClusterStatusReportRequestTest {
    @Test
    public void shouldDeserializeFromJSON() {
        JsonObject jsonObject = new JsonObject();
        JsonObject clusterJSON = new JsonObject();
        clusterJSON.addProperty("go_server_url", "https://go-server/go");
        jsonObject.add("cluster_profile_properties", clusterJSON);

        ClusterStatusReportRequest clusterStatusReportRequest = ClusterStatusReportRequest.fromJSON(jsonObject.toString());

        ClusterStatusReportRequest expected = new ClusterStatusReportRequest(Collections.singletonMap("go_server_url", "https://go-server/go"));
        assertThat(clusterStatusReportRequest, CoreMatchers.is(expected));
    }
}
