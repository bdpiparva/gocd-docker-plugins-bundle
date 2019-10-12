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

import com.google.common.collect.ImmutableSortedMap;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse memory specification. Expected format is:
 * <ul>
 *     <li>a float number</li>
 *     <li>followed by a letter: M, G, T</li>
 * </ul>
 */
public class MemorySpecification {
    private static final Pattern PATTERN = Pattern.compile("(\\d+(\\.\\d+)?)(\\D)");

    private static final Map<String, BigDecimal> SUFFIXES = ImmutableSortedMap.<String, BigDecimal>orderedBy(java.lang.String.CASE_INSENSITIVE_ORDER)
            .put("M", BigDecimal.valueOf(1024L * 1024L))
            .put("G", BigDecimal.valueOf(1024L * 1024L * 1024L))
            .put("T", BigDecimal.valueOf(1024L * 1024L * 1024L * 1024L))
            .build();

    private Long memory;

    MemorySpecification(String memory) {
        this.memory = parse(memory);
    }

    Long getMemory() {
        return memory;
    }

    public static Long parse(String memory) {
        if (StringUtils.isBlank(memory)) {
            return null;
        }

        final Matcher matcher = PATTERN.matcher(memory);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size: " + memory);
        }

        final BigDecimal size = new BigDecimal(matcher.group(1));
        final BigDecimal unit = SUFFIXES.get(matcher.group(3));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid size: " + memory + ". Wrong size unit");
        }

        return size.multiply(unit).longValue();
    }
}
