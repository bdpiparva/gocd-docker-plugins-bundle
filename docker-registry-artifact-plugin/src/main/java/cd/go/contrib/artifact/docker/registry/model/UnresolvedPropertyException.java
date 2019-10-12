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

package cd.go.contrib.artifact.docker.registry.model;

public class UnresolvedPropertyException extends Exception {

    private final String partiallyResolvedTag;

    public UnresolvedPropertyException(String partiallyResolvedTag, String propertyName) {
        super(String.format("Failed to resolve one or more variables in %s: %s", propertyName, partiallyResolvedTag));
        this.partiallyResolvedTag = partiallyResolvedTag;
    }

    public String getPartiallyResolvedTag() {
        return partiallyResolvedTag;
    }
}
