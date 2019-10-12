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

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ClusterProfileValidateRequestTest {
    @Test
    public void shouldDeserializeFromJSON() throws Exception {

        String json ="{" +
                "   \"go_server_url\":\"http://localhost\"," +
                "   \"auto_register_timeout\":\"10\"," +
                "   \"username\":\"Bob\"," +
                "   \"password\":\"secret\"" +
                "}";
        ClusterProfileValidateRequest request = ClusterProfileValidateRequest.fromJSON(json);
        HashMap<String, String> expectedSettings = new HashMap<>();
        expectedSettings.put("go_server_url", "http://localhost");
        expectedSettings.put("auto_register_timeout", "10");
        expectedSettings.put("username", "Bob");
        expectedSettings.put("password", "secret");
        assertThat(request.getProperties(), equalTo(expectedSettings));
    }
}
