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

import org.apache.commons.lang.StringUtils;

/**
 * Parse Cpus value, expected format
 * <ul>
 * <li>float number</li>
 * <li>decimal separator: a dot (.)</li>
 * </ul>
 * The "cpus" settings is not allowed by the Spotify docker client yet.
 * Once the client supports the parameter, this can be simplified.
 * Currently, the "cpus" value is translated as it is written in documentation:
 * <tt>--cpus="1.5"</tt> is equivalent of setting <tt>--cpu-period="100000"</tt>
 * and <tt>--cpu-quota="150000"</tt>.
 *
 * @see <a href="https://docs.docker.com/config/containers/resource_constraints/#cpu">Docker CPU settings</a>.
 */
public class CpusSpecification {
    private Float cpus;

    public CpusSpecification(String cpus) {
        this.cpus = parse(cpus);
    }

    public Float getCpus() {
        return cpus;
    }

    public long getCpuPeriod() {
        return 100_000l;
    }

    public long getCpuQuota() {
        return ((long) (getCpus() * getCpuPeriod()));
    }

    public static Float parse(String cpus) {
        if (StringUtils.isBlank(cpus)) {
            return null;
        }
        return Float.parseFloat(cpus);
    }
}
