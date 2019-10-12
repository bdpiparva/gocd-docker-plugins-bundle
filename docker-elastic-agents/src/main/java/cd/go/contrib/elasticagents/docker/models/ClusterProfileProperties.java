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

import cd.go.contrib.elasticagents.docker.utils.Util;
import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joda.time.Period;

import java.util.Collection;
import java.util.Objects;

import static cd.go.plugin.base.GsonTransformer.fromJson;

@Setter
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
@Accessors(chain = true)
public class ClusterProfileProperties {
    public static final String GO_SERVER_URL = "go_server_url";
    @Expose
    @SerializedName(GO_SERVER_URL)
    @Property(name = GO_SERVER_URL, required = true)
    private String goServerUrl;

    @Expose
    @SerializedName("environment_variables")
    @Property(name = "environment_variables")
    private String environmentVariables;

    @Expose
    @SerializedName("max_docker_containers")
    @Property(name = "max_docker_containers", required = true)
    private String maxDockerContainers;

    @Expose
    @SerializedName("docker_uri")
    @Property(name = "docker_uri", required = true)
    private String dockerURI;

    @Expose
    @SerializedName("auto_register_timeout")
    @Property(name = "auto_register_timeout", required = true)
    private String autoRegisterTimeout;

    @Expose
    @SerializedName("docker_ca_cert")
    @Property(name = "docker_ca_cert")
    private String dockerCACert;

    @Expose
    @SerializedName("docker_client_cert")
    @Property(name = "docker_client_cert")
    private String dockerClientCert;

    @Expose
    @SerializedName("docker_client_key")
    @Property(name = "docker_client_key")
    private String dockerClientKey;

    @Expose
    @SerializedName("private_registry_server")
    @Property(name = "private_registry_server")
    private String privateRegistryServer;

    @Expose
    @SerializedName("private_registry_username")
    @Property(name = "private_registry_username")
    private String privateRegistryUsername;

    @Expose
    @SerializedName("private_registry_password")
    @Property(name = "private_registry_password", secure = true)
    private String privateRegistryPassword;

    @Expose
    @SerializedName("enable_private_registry_authentication")
    @Property(name = "enable_private_registry_authentication")
    private boolean useDockerAuthInfo;

    @Expose
    @SerializedName("private_registry_custom_credentials")
    @Property(name = "private_registry_custom_credentials")
    private boolean useCustomRegistryCredentials;

    @Expose
    @SerializedName("pull_on_container_create")
    @Property(name = "pull_on_container_create")
    private boolean pullOnContainerCreate;

    private Period autoRegisterPeriod;

    public Period getAutoRegisterPeriod() {
        if (this.autoRegisterPeriod == null) {
            this.autoRegisterPeriod = new Period().withMinutes(Integer.parseInt(getAutoRegisterTimeout()));
        }
        return this.autoRegisterPeriod;
    }

    private String getAutoRegisterTimeout() {
        if (autoRegisterTimeout == null) {
            autoRegisterTimeout = "10";
        }
        return autoRegisterTimeout;
    }

    public void setAutoRegisterTimeout(String autoRegisterTimeout) {
        this.autoRegisterTimeout = autoRegisterTimeout;
    }

    public Collection<String> getEnvironmentVariables() {
        return Util.splitIntoLinesAndTrimSpaces(environmentVariables);
    }

    public Integer getMaxDockerContainers() {
        return Integer.valueOf(maxDockerContainers);
    }

    public String getGoServerUrl() {
        return goServerUrl;
    }

    public String getDockerURI() {
        return dockerURI;
    }

    public String getDockerCACert() {
        return dockerCACert;
    }

    public String getDockerClientCert() {
        return dockerClientCert;
    }

    public String getDockerClientKey() {
        return dockerClientKey;
    }

    public String getPrivateRegistryServer() {
        return privateRegistryServer;
    }

    public String getPrivateRegistryUsername() {
        return privateRegistryUsername;
    }

    public String getPrivateRegistryPassword() {
        return privateRegistryPassword;
    }

    public Boolean useDockerAuthInfo() {
        return useDockerAuthInfo;
    }

    public Boolean useCustomRegistryCredentials() {
        return useCustomRegistryCredentials;
    }

    public Boolean pullOnContainerCreate() {
        return pullOnContainerCreate;
    }

    public static ClusterProfileProperties fromJSON(String json) {
        return fromJson(json, ClusterProfileProperties.class);
    }

    public String uuid() {
        return Integer.toHexString(Objects.hash(this));
    }
}
