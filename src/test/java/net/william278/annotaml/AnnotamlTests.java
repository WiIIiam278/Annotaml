package net.william278.annotaml;

import org.junit.jupiter.api.Test;

import java.io.File;

public class AnnotamlTests {

    private final File testFile = new File("./src/test/resources/test.yml");
    private final File deserializeTestFile = new File("./src/test/resources/test_out.yml");

    @Test
    public void testYamlSerialization() {
        Annotaml<TestYamlFile> annotaml = new Annotaml<>(new TestYamlFile(), testFile);
        annotaml.serializeYaml();
    }

    @Test
    public void testYamlDeSerialization() {
        TestYamlFile testYamlFile = Annotaml.load(testFile, TestYamlFile.class);
        new Annotaml<>(testYamlFile, deserializeTestFile).serializeYaml();
    }

}
