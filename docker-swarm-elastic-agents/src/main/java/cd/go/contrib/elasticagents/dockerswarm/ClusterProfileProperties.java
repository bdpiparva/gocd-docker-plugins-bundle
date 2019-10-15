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

import cd.go.contrib.elasticagents.common.models.ClusterProfileConfiguration;
import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.spotify.docker.client.messages.RegistryAuth;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joda.time.Period;

import java.util.Collection;
import java.util.Objects;

@Setter
@Accessors(chain = true)
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class ClusterProfileProperties implements ClusterProfileConfiguration {
    public static final String ENABLE_PRIVATE_REGISTRY_AUTHENTICATION = "enable_private_registry_authentication";
    public static final String PRIVATE_REGISTRY_PASSWORD = "private_registry_password";
    public static final String PRIVATE_REGISTRY_USERNAME = "private_registry_username";
    public static final String PRIVATE_REGISTRY_SERVER = "private_registry_server";
    @Expose
    @SerializedName("go_server_url")
    @Property(name = "go_server_url", required = true)
    private String goServerUrl;

    @Expose
    @Property(name = "environment_variables")
    @SerializedName("environment_variables")
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
    @Property(name = "docker_ca_cert", secure = true)
    private String dockerCACert;

    @Expose
    @SerializedName("docker_client_cert")
    @Property(name = "docker_client_cert", secure = true)
    private String dockerClientCert;

    @Expose
    @SerializedName("docker_client_key")
    @Property(name = "docker_client_key", secure = true)
    private String dockerClientKey;

    @Expose
    @SerializedName(PRIVATE_REGISTRY_SERVER)
    @Property(name = PRIVATE_REGISTRY_SERVER)
    private String privateRegistryServer;

    @Expose
    @SerializedName(PRIVATE_REGISTRY_USERNAME)
    @Property(name = PRIVATE_REGISTRY_USERNAME)
    private String privateRegistryUsername;

    @Expose
    @SerializedName(PRIVATE_REGISTRY_PASSWORD)
    @Property(name = PRIVATE_REGISTRY_PASSWORD, secure = true)
    private String privateRegistryPassword;

    @Expose
    @SerializedName(ENABLE_PRIVATE_REGISTRY_AUTHENTICATION)
    @Property(name = ENABLE_PRIVATE_REGISTRY_AUTHENTICATION)
    private String useDockerAuthInfo;

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

    public boolean useDockerAuthInfo() {
        return Boolean.valueOf(useDockerAuthInfo);
    }

    public RegistryAuth registryAuth() {
        return RegistryAuth.builder()
                .serverAddress(privateRegistryServer)
                .username(privateRegistryUsername)
                .password(privateRegistryPassword)
                .build();
    }

    public String uuid() {
        return Integer.toHexString(Objects.hash(this));
    }
}
