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

package cd.go.contrib.elasticagents.dockerswarm.utils;

import com.google.common.collect.Collections2;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.spotify.docker.client.VersionCompare.compareVersion;
import static org.apache.commons.lang.StringUtils.isBlank;


public class Util {
    public static Collection<String> splitIntoLinesAndTrimSpaces(String lines) {
        if (isBlank(lines)) {
            return Collections.emptyList();
        }

        return Collections2.transform(Arrays.asList(lines.split("[\r\n]+")), input -> input.trim());
    }

    public static List<String> linesToList(String lines) {
        return new ArrayList<>(splitIntoLinesAndTrimSpaces(lines));
    }

    public static Map<String, String> linesToMap(String lines) {
        return splitIntoLinesAndTrimSpaces(lines).stream()
                .map(line -> line.split("="))
                .collect(Collectors.toMap(e -> e[0].trim(), e -> e[1].trim()));
    }

    public static boolean dockerApiVersionAtLeast(DockerClient docker,
                                                  final String expected) throws DockerException, InterruptedException {
        return compareVersion(docker.version().apiVersion(), expected) >= 0;
    }

    public static String readableSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
