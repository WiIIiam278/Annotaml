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

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@YamlFile(header = "Hello, world! \nTest header comment!\nThird line")
public class TestYamlFile {

    public String test = "test";
    public int test2 = 2;
    @YamlKey("test3.nested.test")
    public boolean test3 = true;
    @YamlComment("Test comment")
    public double test4 = 4.0;
    @YamlKey("test_5")
    public float test5 = 5.0f;
    @YamlIgnored
    public String ignored = "ignored";
    public TestEnum testEnum = TestEnum.TEST2;
    public TestEnum testCasedEnum = TestEnum.TEST3;
    public Map<TestEnum, String> test6 = Map.of(TestEnum.TEST, "test",
            TestEnum.TEST2, "test2",
            TestEnum.TEST3, "test3");
    public Map<String, String> test7 = Map.of("test", "value",
            "test2", "value2",
            "test3", "value3",
            "test4", "value4",
            "test5", "value5");
    public List<String> list = List.of("test", "test2", "test3");

    public TestYamlFile() {
    }

    public enum TestEnum {
        TEST,
        TEST2,
        TEST3
    }
}
