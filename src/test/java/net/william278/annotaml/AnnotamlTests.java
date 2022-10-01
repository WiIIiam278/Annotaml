package net.william278.annotaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public class AnnotamlTests {

    @Test
    public void testWrite() throws IOException {
        // Write a file to the temp directory
        Annotaml.create(new TestYamlFile()).save(
                new File(System.getProperty("java.io.tmpdir"), "test_write.yml"));
    }

    @Test
    public void testRead() {
        try (FileInputStream input = new FileInputStream("C:/Users/William/IdeaProjects/Annotaml/src/test/resources/file.yml")) {
            final TestYamlFile readFile = (TestYamlFile) Annotaml.create(new TestYamlFile(), input).get();
            Assertions.assertEquals("test", readFile.test);
            Assertions.assertTrue(readFile.test3);
            Assertions.assertEquals(3, readFile.test6.size());
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadNoDefaults() {
        try (FileInputStream input = new FileInputStream("C:/Users/William/IdeaProjects/Annotaml/src/test/resources/file_no_defaults.yml")) {
            final TestYamlNoDefaultsFile noDefaultsFile = (TestYamlNoDefaultsFile) Annotaml.create(TestYamlNoDefaultsFile.class, input).get();
            Assertions.assertEquals("Hello", noDefaultsFile.value1);
            Assertions.assertEquals(33.3, noDefaultsFile.value2);
            Assertions.assertTrue(noDefaultsFile.value3);
            Assertions.assertEquals(3, noDefaultsFile.value4.size());
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
