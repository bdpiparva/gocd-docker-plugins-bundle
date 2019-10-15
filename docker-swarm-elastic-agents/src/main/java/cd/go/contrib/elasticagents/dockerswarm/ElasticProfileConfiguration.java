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
@EqualsAndHashCode(doNotUseGetters = true)
@Accessors(chain = true)
public class ElasticProfileConfiguration implements cd.go.contrib.elasticagents.common.models.ElasticProfileConfiguration {
    @Expose
    @SerializedName("Image")
    @Property(name = "Image", required = true)
    private String image;

    @Expose
    @SerializedName("Command")
    @Property(name = "Command")
    private String command;

    @Expose
    @Property(name = "Environment")
    @SerializedName("Environment")
    private String environment;

    @Expose
    @Property(name = "Secrets")
    @SerializedName("Secrets")
    private String secrets;

    @Expose
    @Property(name = "Networks")
    @SerializedName("Networks")
    private String networks;

    @Expose
    @Property(name = "Mounts")
    @SerializedName("Mounts")
    private String mounts;

    @Expose
    @Property(name = "Constraints")
    @SerializedName("Constraints")
    private String constraints;

    @Expose
    @Property(name = "LogDriver")
    @SerializedName("LogDriver")
    private String logDriver;

    @Expose
    @Property(name = "LogDriverOptions")
    @SerializedName("LogDriverOptions")
    private String logDriverOptions;

    @Expose
    @Property(name = "MaxMemory")
    @SerializedName("MaxMemory")
    private String maxMemory;

    @Expose
    @Property(name = "ReservedMemory")
    @SerializedName("ReservedMemory")
    private String reservedMemory;

    @Expose
    @Property(name = "Hosts")
    @SerializedName("Hosts")
    private String hosts;

}
