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

package cd.go.contrib.elasticagents.common;

import cd.go.contrib.elasticagents.common.models.JobIdentifier;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElasticAgentRequestClientTest {
    @Mock
    private GoApplicationAccessor accessor;
    @Mock
    private GoPluginIdentifier pluginIdentifier;
    private JobIdentifier jobIdentifier;
    private ElasticAgentRequestClient pluginRequest;

    @BeforeEach
    void setUp() {
        jobIdentifier = new JobIdentifier("p1", 1L, "l1", "s1", "1", "j1", 1L);
        accessor = mock(GoApplicationAccessor.class);
        pluginRequest = new ElasticAgentRequestClient(accessor, "1.0", pluginIdentifier);

    }

    @Test
    void shouldNotThrowAnExceptionIfConsoleLogAppenderCallFails() {
        when(accessor.submit(any())).thenReturn(DefaultGoApiResponse.badRequest("Something went wrong"));

        pluginRequest.appendToConsoleLog(jobIdentifier, "text1");
    }

}
