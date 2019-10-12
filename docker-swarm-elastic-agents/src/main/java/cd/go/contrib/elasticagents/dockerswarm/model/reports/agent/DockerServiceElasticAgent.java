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

package cd.go.contrib.elasticagents.dockerswarm.model.reports.agent;

import cd.go.contrib.elasticagents.dockerswarm.model.JobIdentifier;
import cd.go.contrib.elasticagents.dockerswarm.utils.Util;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Resources;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static cd.go.contrib.elasticagents.dockerswarm.Constants.JOB_IDENTIFIER_LABEL_KEY;

public class DockerServiceElasticAgent {
    private String id;
    private String name;
    private Date createdAt;
    private String logs;
    private String limits;
    private String reservations;
    private String image;
    private String command;
    private String args;
    private JobIdentifier jobIdentifier;
    private String placementConstraints;
    private Map<String, String> environments;
    private String hosts;
    private String hostname;
    private List<TaskStatus> tasksStatus = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getLogs() {
        return logs;
    }

    public String getLimits() {
        return limits;
    }

    public String getReservations() {
        return reservations;
    }

    public String getImage() {
        return image;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public String getPlacementConstraints() {
        return placementConstraints;
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }

    public String getHosts() {
        return hosts;
    }

    public String getHostname() {
        return hostname;
    }

    public List<TaskStatus> getTasksStatus() {
        return tasksStatus;
    }

    public static DockerServiceElasticAgent fromService(Service service, DockerClient client) throws DockerException, InterruptedException {
        DockerServiceElasticAgent agent = new DockerServiceElasticAgent();

        agent.id = service.id();
        agent.name = service.spec().name();
        agent.createdAt = service.createdAt();
        agent.jobIdentifier = JobIdentifier.fromJson(service.spec().labels().get(JOB_IDENTIFIER_LABEL_KEY));

        LogStream logStream = client.serviceLogs(service.id(), DockerClient.LogsParam.stdout(), DockerClient.LogsParam.stderr());
        agent.logs = logStream.readFully();
        logStream.close();

        TaskSpec taskSpec = service.spec().taskTemplate();

        agent.image = taskSpec.containerSpec().image();
        agent.hostname = taskSpec.containerSpec().hostname();
        agent.limits = resourceToString(taskSpec.resources().limits());
        agent.reservations = resourceToString(taskSpec.resources().reservations());
        agent.command = listToString(taskSpec.containerSpec().command());
        agent.args = listToString(taskSpec.containerSpec().args());
        agent.placementConstraints = listToString(taskSpec.placement().constraints());
        agent.environments = toMap(taskSpec);
        agent.hosts = listToString(taskSpec.containerSpec().hosts());

        final List<Task> tasks = client.listTasks(Task.Criteria.builder().serviceName(service.id()).build());
        if (!tasks.isEmpty()) {
            for (Task task : tasks) {
                agent.tasksStatus.add(new TaskStatus(task));
            }
        }

        return agent;
    }

    private static Map<String, String> toMap(TaskSpec taskSpec) {
        final List<String> envFromTask = taskSpec.containerSpec().env();
        Map<String, String> envs = new HashMap<>();
        if (envFromTask != null) {
            for (String env : envFromTask) {
                final String[] parts = env.split("=", 2);
                
                if ("GO_EA_AUTO_REGISTER_KEY".equals(parts[0])) {
                    continue;
                }

                if (parts.length == 2) {
                    envs.put(parts[0], parts[1]);
                } else {
                    envs.put(parts[0], null);
                }
            }
        }
        return envs;
    }

    private static String listToString(List<String> stringList) {
        return (stringList == null || stringList.isEmpty()) ? "Not Specified" : StringUtils.join(stringList, "\n");
    }

    private static String resourceToString(Resources resource) {
        return (resource == null) ? "Not Specified" : Util.readableSize(resource.memoryBytes());
    }
}
