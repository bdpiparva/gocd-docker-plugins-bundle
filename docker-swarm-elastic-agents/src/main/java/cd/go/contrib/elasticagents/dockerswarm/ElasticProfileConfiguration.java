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

package cd.go.contrib.elasticagents.dockerswarm;

import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class ElasticProfileConfiguration implements cd.go.contrib.elasticagents.common.models.ElasticProfileConfiguration {
    @Property(name = "Image", required = true)
    @SerializedName("Image")
    @Expose
    private String image;
    @Property(name = "Command")
    @SerializedName("Command")
    @Expose
    private String command;
    @Property(name = "Environment")
    @SerializedName("Environment")
    @Expose
    private String environment;
    @Property(name = "Secrets")
    @SerializedName("Secrets")
    @Expose
    private String secrets;
    @Property(name = "Networks")
    @SerializedName("Networks")
    @Expose
    private String networks;
    @Property(name = "Mounts")
    @SerializedName("Mounts")
    @Expose
    private String mounts;

    @Property(name = "Constraints")
    @SerializedName("Constraints")
    @Expose
    private String constraints;
    @Property(name = "LogDriver")
    @SerializedName("LogDriver")
    @Expose
    private String logDriver;
    @Property(name = "LogDriverOptions")
    @SerializedName("LogDriverOptions")
    @Expose
    private String logDriverOptions;

    @Property(name = "MaxMemory")
    @SerializedName("MaxMemory")
    @Expose
    private String maxMemory;

    @Property(name = "ReservedMemory")
    @SerializedName("ReservedMemory")
    @Expose
    private String reservedMemory;

    @Expose
    @Property(name = "Hosts")
    @SerializedName("Hosts")
    private String hosts;

}
