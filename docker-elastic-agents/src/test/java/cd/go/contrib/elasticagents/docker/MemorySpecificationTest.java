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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MemorySpecificationTest {
    @Test
    void memorySpecificationIsParsedCorrectly() {
        assertThat(Long.valueOf(10L * 1024L * 1024L)).isEqualTo(new MemorySpecification("10M").getMemory());
        assertThat(Long.valueOf(25L * 1024L * 1024L * 1024L)).isEqualTo(new MemorySpecification("25G").getMemory());
        assertThat(Long.valueOf(138L * 1024L * 1024L * 1024L * 1024L)).isEqualTo(new MemorySpecification("138T").getMemory());
        assertThat(Long.valueOf(15L * 1024L * 1024L / 10L)).isEqualTo(new MemorySpecification("1.5M").getMemory());
    }

    @Test
    void shouldBombWhenParsingErrors() {
        assertThatCode(() -> new MemorySpecification("5K")).hasMessageContaining("Invalid size: 5K. Wrong size unit");
        assertThatCode(() -> new MemorySpecification("5")).hasMessageContaining("Invalid size: 5");
        assertThatCode(() -> new MemorySpecification("A")).hasMessageContaining("Invalid size: A");
        assertThatCode(() -> new MemorySpecification(".3M")).hasMessageContaining("Invalid size: .3M");
        assertThatCode(() -> new MemorySpecification("1,3M")).hasMessageContaining("Invalid size: 1,3M");
    }
}
