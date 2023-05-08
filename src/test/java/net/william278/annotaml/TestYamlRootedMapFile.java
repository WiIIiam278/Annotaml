/*
 * This file is part of Annotaml, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.annotaml;

import java.util.Map;

@YamlFile(header = "Tests for the rooted map implementation", rootedMap = true)
public class TestYamlRootedMapFile {

    public Map<String, String> rootedMap = Map.of("test", "value",
            "test2", "value2",
            "test3", "value3",
            "test4", "value4",
            "test5", "value5");

    public TestYamlRootedMapFile() {
    }
}
