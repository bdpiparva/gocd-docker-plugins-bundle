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

package cd.go.contrib.elasticagents.docker;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class CpusSpecificationTest {
    @Test
    public void cpusIsTranslatedToCpuPeriodAndCpuQuota() {
        CpusSpecification cpus1 = new CpusSpecification("1.5");

        assertThat(cpus1.getCpuPeriod()).isEqualTo(100_000L);
        assertThat(cpus1.getCpuQuota()).isEqualTo(150_000L);

        CpusSpecification cpus2 = new CpusSpecification(".5");

        assertThat(cpus2.getCpuPeriod()).isEqualTo(100_000L);
        assertThat(cpus2.getCpuQuota()).isEqualTo(50_000L);
    }

    @Test
    public void cpusParsedWithErrors() {
        assertThatCode(() -> new CpusSpecification("0,3"))
                .hasMessage("For input string: \"0,3\"");
        assertThatCode(() -> new CpusSpecification("abc"))
                .hasMessage("For input string: \"abc\"");
    }
}
