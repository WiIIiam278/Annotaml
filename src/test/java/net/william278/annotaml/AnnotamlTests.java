package net.william278.annotaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class AnnotamlTests {

    @Test
    public void testWrite() throws IOException {
        final File file = new File(System.getProperty("java.io.tmpdir"), "test_write.yml");

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
}
