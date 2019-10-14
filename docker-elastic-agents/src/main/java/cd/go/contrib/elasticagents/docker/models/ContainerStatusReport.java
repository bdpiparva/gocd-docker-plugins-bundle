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
import com.google.gson.annotations.Expose;

public class ContainerStatusReport {
    @Expose
    private String id;
    @Expose
    private String image;
    @Expose
    private String state;
    @Expose
    private Long createdAt;
    @Expose
    private final JobIdentifier jobIdentifier;
    @Expose
    private final String elasticAgentId;

    public ContainerStatusReport(String id,
                                 String image,
                                 String state,
                                 Long createdAt,
                                 JobIdentifier jobIdentifier,
                                 String elasticAgentId) {
        this.id = id;
        this.image = image;
        this.state = state;
        this.createdAt = createdAt;
        this.jobIdentifier = jobIdentifier;
        this.elasticAgentId = elasticAgentId;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getState() {
        return state;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }
}
