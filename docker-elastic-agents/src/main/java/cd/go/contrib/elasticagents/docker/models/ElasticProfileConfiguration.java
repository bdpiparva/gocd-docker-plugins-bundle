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

import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@Accessors(chain = true)
public
class ElasticProfileConfiguration {
    public static final String IMAGE = "Image";
    public static final String COMMAND = "Command";
    public static final String ENVIRONMENT = "Environment";
    public static final String RESERVED_MEMORY = "ReservedMemory";
    public static final String MAX_MEMORY = "MaxMemory";
    public static final String CPUS = "Cpus";
    public static final String MOUNTS = "Mounts";
    public static final String HOSTS = "Hosts";
    public static final String PRIVILEGED = "Privileged";
    @Expose
    @SerializedName(IMAGE)
    @Property(name = IMAGE, required = true)
    private String image;

    @Expose
    @SerializedName(COMMAND)
    @Property(name = COMMAND)
    private String command;

    @Expose
    @SerializedName(ENVIRONMENT)
    @Property(name = ENVIRONMENT)
    private String environmentVariables;

    @Expose
    @SerializedName(RESERVED_MEMORY)
    @Property(name = RESERVED_MEMORY)
    private String reservedMemory;

    @Expose
    @SerializedName(MAX_MEMORY)
    @Property(name = MAX_MEMORY)
    private String maxMemory;

    @Expose
    @SerializedName(CPUS)
    @Property(name = CPUS)
    private String cpus;

    @Expose
    @SerializedName(MOUNTS)
    @Property(name = MOUNTS)
    private String mounts;

    @Expose
    @SerializedName(HOSTS)
    @Property(name = HOSTS)
    private String hosts;

    @Expose
    @SerializedName(PRIVILEGED)
    @Property(name = PRIVILEGED)
    private String privileged;
}
