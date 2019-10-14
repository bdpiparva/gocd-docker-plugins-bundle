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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.spotify.docker.client.messages.RegistryAuth;
import lombok.Setter;
import org.joda.time.Period;

import java.util.Collection;
import java.util.Objects;

@Setter
public class ClusterProfileProperties implements ClusterProfileConfiguration {
    @Expose
    @SerializedName("go_server_url")
    private String goServerUrl;

    @Expose
    @SerializedName("environment_variables")
    private String environmentVariables;

    @Expose
    @SerializedName("max_docker_containers")
    private String maxDockerContainers;

    @Expose
    @SerializedName("docker_uri")
    private String dockerURI;

    @Expose
    @SerializedName("auto_register_timeout")
    private String autoRegisterTimeout;

    @Expose
    @SerializedName("docker_ca_cert")
    private String dockerCACert;

    @Expose
    @SerializedName("docker_client_cert")
    private String dockerClientCert;

    @Expose
    @SerializedName("docker_client_key")
    private String dockerClientKey;

    @Expose
    @SerializedName("private_registry_server")
    private String privateRegistryServer;

    @Expose
    @SerializedName("private_registry_username")
    private String privateRegistryUsername;

    @Expose
    @SerializedName("private_registry_password")
    private String privateRegistryPassword;

    @Expose
    @SerializedName("enable_private_registry_authentication")
    private boolean useDockerAuthInfo;

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

    @Override
    public String toString() {
        return super.toString();
    }

}
