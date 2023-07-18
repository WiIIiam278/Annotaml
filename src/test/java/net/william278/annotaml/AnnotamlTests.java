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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class AnnotamlTests {

    @Test
    public void testWrite() throws IOException {
        final File file = new File(System.getProperty("java.io.tmpdir"), "file.yml");

        // If the file exists, delete
        if (file.exists()) {
            Assertions.assertTrue(file.delete());
        }

        // Write a file to the temp directory
        Annotaml.create(new TestYamlFile()).save(file);
    }

    @Test
    public void testRead() {
        try (InputStream input = Objects.requireNonNull(getClass().getClassLoader().getResource("file.yml")).openStream()) {
            final TestYamlFile readFile = Annotaml.create(new TestYamlFile(), input).get();
            Assertions.assertEquals("test", readFile.test);
            Assertions.assertTrue(readFile.test3);
            Assertions.assertEquals(3, readFile.test6.size());
            Assertions.assertEquals(3, readFile.test7.size());
            Assertions.assertEquals("three", readFile.test7.get("test3"));
            Assertions.assertEquals(readFile.testCasedEnum, TestYamlFile.TestEnum.TEST3);
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadNoDefaults() {
        try (InputStream input = Objects.requireNonNull(getClass().getClassLoader().getResource("file_no_defaults.yml")).openStream()) {
            final TestYamlNoDefaultsFile noDefaultsFile = Annotaml.create(TestYamlNoDefaultsFile.class, input).get();
            Assertions.assertEquals("Hello", noDefaultsFile.value1);
            Assertions.assertEquals(33.3, noDefaultsFile.value2);
            Assertions.assertTrue(noDefaultsFile.value3);
            Assertions.assertEquals(3, noDefaultsFile.value4.size());
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadYamlRootedMap() {
        try (InputStream input = Objects.requireNonNull(getClass().getClassLoader().getResource("file_rooted_map.yml")).openStream()) {
            final TestYamlRootedMapFile rootedMapFile = Annotaml.create(TestYamlRootedMapFile.class, input).get();
            Assertions.assertEquals(3, rootedMapFile.rootedMap.size());
            Assertions.assertEquals("value1", rootedMapFile.rootedMap.get("test1"));
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWriteYamlRootedMap() throws IOException {
        final File file = new File(System.getProperty("java.io.tmpdir"), "test_write_rooted_map.yml");

        // If the file exists, delete
        if (file.exists()) {
            Assertions.assertTrue(file.delete());
        }

        // Write a file to the temp directory
        Annotaml.create(new TestYamlRootedMapFile()).save(file);
    }
}
