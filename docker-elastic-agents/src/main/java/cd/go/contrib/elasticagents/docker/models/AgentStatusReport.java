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

import cd.go.contrib.elasticagents.common.JobIdentifier;

import java.util.List;
import java.util.Map;

public class AgentStatusReport {
    private final JobIdentifier jobIdentifier;
    private final String elasticAgentId;
    private final Long createdAt;
    private final String image;
    private final String command;
    private final String ipAddress;
    private final String logs;
    private final Map<String, String> environmentVariables;
    private final List<String> hosts;

    public AgentStatusReport(JobIdentifier jobIdentifier,
                             String elasticAgentId,
                             Long createdAt,
                             String image,
                             String command,
                             String ipAddress,
                             String logs,
                             Map<String, String> environmentVariables,
                             List<String> hosts) {
        this.jobIdentifier = jobIdentifier;
        this.elasticAgentId = elasticAgentId;
        this.createdAt = createdAt;
        this.image = image;
        this.command = command;
        this.ipAddress = ipAddress;
        this.logs = logs;
        this.environmentVariables = environmentVariables;
        this.hosts = hosts;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLogs() {
        return logs;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<String> getHosts() {
        return hosts;
    }
}
