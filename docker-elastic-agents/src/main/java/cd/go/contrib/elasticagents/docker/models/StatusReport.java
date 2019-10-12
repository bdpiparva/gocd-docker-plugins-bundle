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

import com.google.gson.annotations.Expose;

import java.util.List;

public class StatusReport {
    @Expose
    private final Integer cpus;
    @Expose
    private final String memory;
    @Expose
    private final String os;
    @Expose
    private final String architecture;
    @Expose
    private final String dockerVersion;
    @Expose
    private final List<ContainerStatusReport> containerStatusReports;

    public StatusReport(String os, String architecture, String dockerVersion, Integer cpus, String memory,
                        List<ContainerStatusReport> containerStatusReports) {
        this.os = os;
        this.architecture = architecture;
        this.dockerVersion = dockerVersion;
        this.cpus = cpus;
        this.memory = memory;
        this.containerStatusReports = containerStatusReports;
    }

    public Integer getCpus() {
        return cpus;
    }

    public String getMemory() {
        return memory;
    }

    public String getOs() {
        return os;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public List<ContainerStatusReport> getContainerStatusReports() {
        return containerStatusReports;
    }
}
