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
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import static java.text.MessageFormat.format;

@ToString
@EqualsAndHashCode
public class JobIdentifier {
    @Expose
    @SerializedName("pipeline_name")
    private String pipelineName;

    @Expose
    @SerializedName("pipeline_counter")
    private Long pipelineCounter;

    @Expose
    @SerializedName("pipeline_label")
    private String pipelineLabel;

    @Expose
    @SerializedName("stage_name")
    private String stageName;

    @Expose
    @SerializedName("stage_counter")
    private String stageCounter;

    @Expose
    @SerializedName("job_name")
    private String jobName;

    @Expose
    @SerializedName("job_id")
    private Long jobId;

    private String representation;

    public JobIdentifier(Long jobId) {
        this.jobId = jobId;
    }

    public JobIdentifier() {
    }

    public JobIdentifier(String pipelineName,
                         Long pipelineCounter,
                         String pipelineLabel,
                         String stageName,
                         String stageCounter,
                         String jobName,
                         Long jobId) {
        this.pipelineName = pipelineName;
        this.pipelineCounter = pipelineCounter;
        this.pipelineLabel = pipelineLabel;
        this.stageName = stageName;
        this.stageCounter = stageCounter;
        this.jobName = jobName;
        this.jobId = jobId;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public Long getPipelineCounter() {
        return pipelineCounter;
    }

    public String getPipelineLabel() {
        return pipelineLabel;
    }

    public String getStageName() {
        return stageName;
    }

    public String getStageCounter() {
        return stageCounter;
    }

    public String getRepresentation() {
        if (StringUtils.isBlank(representation)) {
            this.representation = format("{0}/{1}/{2}/{3}/{4}", pipelineName, pipelineCounter, stageName, stageCounter, jobName);
        }
        return representation;
    }

    public String represent() {
        return String.format("%s/%d/%s/%s/%s", pipelineName, pipelineCounter, stageName, stageCounter, jobName);
    }
}
