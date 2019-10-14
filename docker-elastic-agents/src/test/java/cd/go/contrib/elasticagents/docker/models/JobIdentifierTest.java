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

package cd.go.contrib.elasticagents.docker.models;

import cd.go.contrib.elasticagents.docker.utils.JobIdentifierMother;
import cd.go.contrib.elasticagents.common.JobIdentifier;
import org.junit.jupiter.api.Test;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static org.assertj.core.api.Assertions.assertThat;

class JobIdentifierTest {

    @Test
    void shouldDeserializeFromJson() {
        JobIdentifier jobIdentifier = fromJson(JobIdentifierMother.getJson().toString(), JobIdentifier.class);

        JobIdentifier expected = JobIdentifierMother.get();
        assertThat(jobIdentifier).isEqualTo(expected);
    }

    @Test
    void shouldGetRepresentation() {
        String representation = JobIdentifierMother.get().getRepresentation();

        assertThat(representation).isEqualTo("up42/1/stage/1/job1");
    }

}
