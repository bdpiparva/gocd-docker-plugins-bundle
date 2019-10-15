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

import com.spotify.docker.client.messages.swarm.Secret;
import com.spotify.docker.client.messages.swarm.SecretBind;
import com.spotify.docker.client.messages.swarm.SecretSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DockerSecretsTest {

    @Test
    void shouldBuildDockerSecretFromString() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=Foo, uid=uid,gid=gid, mode=640");

        assertThat(dockerSecrets).isNotNull();
        assertThat(dockerSecrets).hasSize(1);

        assertThat(dockerSecrets.get(0).name()).isEqualTo("Username");
        assertThat(dockerSecrets.get(0).file()).isEqualTo("Foo");
        assertThat(dockerSecrets.get(0).uid()).isEqualTo("uid");
        assertThat(dockerSecrets.get(0).gid()).isEqualTo("gid");
        assertThat(dockerSecrets.get(0).mode()).isEqualTo(0640L);
    }

    @Test
    void shouldSkipEmptyLine() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=Foo, uid=UID\n\nsrc=Password, target=Bar");

        assertThat(dockerSecrets).isNotNull();
        assertThat(dockerSecrets).hasSize(2);

        assertThat(dockerSecrets.get(0).name()).isEqualTo("Username");
        assertThat(dockerSecrets.get(1).name()).isEqualTo("Password");
    }

    @Test
    void shouldBuildSecretBindFromDockerSecret() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username, target=username, uid=uid, gid=gid, mode=0640\nsrc=Password, target=passwd, uid=uid, gid=gid, mode=0640");
        final Secret secretForUsername = mock(Secret.class);
        final Secret secretForPassword = mock(Secret.class);

        when(secretForUsername.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secretForUsername.id()).thenReturn("username-secret-id");

        when(secretForPassword.secretSpec()).thenReturn(SecretSpec.builder().name("Password").build());
        when(secretForPassword.id()).thenReturn("password-secret-id");

        final List<SecretBind> secretBinds = dockerSecrets.toSecretBind(asList(secretForUsername, secretForPassword));

        assertThat(secretBinds).hasSize(2);
        assertThat(secretBinds.get(0).secretName()).isEqualTo("Username");
        assertThat(secretBinds.get(0).secretId()).isEqualTo("username-secret-id");
        assertThat(secretBinds.get(0).file().name()).isEqualTo("username");
        assertThat(secretBinds.get(0).file().uid()).isEqualTo("uid");
        assertThat(secretBinds.get(0).file().gid()).isEqualTo("gid");
        assertThat(secretBinds.get(0).file().mode()).isEqualTo(0640L);

        assertThat(secretBinds.get(1).secretName()).isEqualTo("Password");
        assertThat(secretBinds.get(1).secretId()).isEqualTo("password-secret-id");
        assertThat(secretBinds.get(1).file().name()).isEqualTo("passwd");
        assertThat(secretBinds.get(1).file().uid()).isEqualTo("uid");
        assertThat(secretBinds.get(1).file().gid()).isEqualTo("gid");
        assertThat(secretBinds.get(1).file().mode()).isEqualTo(0640L);
    }

    @Test
    void shouldBuildSecretBindFromDockerSecretAndUseDefaultsWhenNotProvided() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username");
        final Secret secret = mock(Secret.class);

        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secret.id()).thenReturn("secret-id");

        final List<SecretBind> secretBinds = dockerSecrets.toSecretBind(asList(secret));

        assertThat(secretBinds).hasSize(1);
        assertThat(secretBinds.get(0).secretName()).isEqualTo("Username");
        assertThat(secretBinds.get(0).secretId()).isEqualTo("secret-id");

        assertThat(secretBinds.get(0).file().name()).isEqualTo("Username");
        assertThat(secretBinds.get(0).file().uid()).isEqualTo("0");
        assertThat(secretBinds.get(0).file().gid()).isEqualTo("0");
        assertThat(secretBinds.get(0).file().mode()).isEqualTo(0444L);
    }

    @Test
    void shouldErrorOutWhenSecretDoesNotExist() {
        final DockerSecrets dockerSecrets = DockerSecrets.fromString("src=Username\nsrc=Password");
        final Secret secret = mock(Secret.class);

        when(secret.secretSpec()).thenReturn(SecretSpec.builder().name("Username").build());
        when(secret.id()).thenReturn("secret-id");

        assertThatCode(() -> dockerSecrets.toSecretBind(of(secret)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Secret with name `Password` does not exist.");
    }

    @Test
    void shouldErrorOutWhenSecretNameIsNotProvided() {
        assertThatCode(() -> DockerSecrets.fromString("target=Username"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid secret specification `target=Username`. Must specify property `src` with value.");
    }

    @Test
    void shouldErrorOutWhenModeIsInvalid() {
        assertThatCode(() -> DockerSecrets.fromString("src=Username, mode=0898").get(0).mode())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid mode value `0898` for secret `Username`. Mode value must be provided in octal.");
        ;
    }
}
