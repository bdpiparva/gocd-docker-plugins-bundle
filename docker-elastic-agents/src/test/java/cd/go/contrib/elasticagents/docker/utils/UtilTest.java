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

package cd.go.contrib.elasticagents.docker.utils;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UtilTest {

    @Test
    void shouldSplitIntoLinesAndTrimSpaces() throws Exception {
        Collection<String> strings = Util.splitIntoLinesAndTrimSpaces("FOO=BAR\n" +
                "  X=Y\n" +
                "\n" +
                "  A=B\r\n" +
                "\n" +
                "W=1");

        assertThat(strings.size()).isEqualTo(4);
        assertThat(strings).contains("FOO=BAR", "X=Y", "A=B", "W=1");
    }
}
