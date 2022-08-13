package net.william278.annotaml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class AnnotamlTests {

    private final Object[] objects = new Object[]{new SampleYaml(), new SampleWithEmbeddedYaml(),
            new SampleRootedMapYaml(), new SampleConfigYaml()};

    // Iterates through test objects to test serialization
    @Test
    public void testObjectSerialization() {
        for (final Object file : objects) {
            Annotaml.save(file, new File(new File(System.getProperty("java.io.tmpdir")),
                    Annotaml.convertToSnakeCase(file.getClass().getSimpleName()) + ".yml"));
        }
    }

    @Test
    public void testObjectDeserialization() {
        for (final Object file : objects) {
            final String fileName = Annotaml.convertToSnakeCase(file.getClass().getSimpleName() + ".yml");
            final Object loaded = Annotaml.load(Objects.requireNonNull(getClass()
                    .getClassLoader().getResourceAsStream(fileName)), file.getClass());
            Annotaml.save(loaded, new File(new File(System.getProperty("java.io.tmpdir")),
                    Annotaml.convertToSnakeCase(file.getClass().getSimpleName()) + ".yml"));
        }
    }

    @Test
    public void testObjectVersioning() {
        final Optional<Integer> versionNumber = Annotaml.getVersionNumber(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream(Annotaml.convertToSnakeCase(SampleConfigYaml.class.getSimpleName() + ".yml"))),
                SampleConfigYaml.class);
        Assertions.assertTrue(versionNumber.isPresent());
        Assertions.assertEquals(1, versionNumber.get().intValue());
    }

    @Test
    public void testUpdating() {
        Annotaml.reload(new File("./src/test/resources/embedded_object_reading_test.yml"), new SampleWithEmbeddedYaml(),
                Annotaml.LoaderOptions.builder().copyDefaults(true));
    }

    @Test
    public void testWrite() {
        Annotaml.save(new SampleWithEmbeddedYaml(),
                new File("./src/test/resources/embedded_object_reading_test.yml"));
    }

    @Test
    public void testLoadingEmbeddedYamlObjects() {
        SampleWithEmbeddedYaml embeddedYamlObject = Annotaml.load(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(
                        "embedded_object_reading_test.yml")), SampleWithEmbeddedYaml.class);
        Assertions.assertNotNull(embeddedYamlObject);
        Assertions.assertNotNull(embeddedYamlObject.embeddedObject);

        Assertions.assertTrue(embeddedYamlObject.embeddedObject.aBoolean);
        Assertions.assertEquals(0.0d, embeddedYamlObject.embeddedObject.aDouble);
        Assertions.assertEquals(1.0f, embeddedYamlObject.embeddedObject.aFloat);
        Assertions.assertEquals(1, embeddedYamlObject.embeddedObject.aLong);
        Assertions.assertNull(embeddedYamlObject.embeddedObject.aString);

        Assertions.assertNotNull(embeddedYamlObject.embeddedObjectList);
        Assertions.assertNotNull(embeddedYamlObject.embeddedObjectMap);

        for (final SampleWithEmbeddedYaml.SampleEmbeddedObject sampleEmbeddedObject : embeddedYamlObject.embeddedObjectList) {
            Assertions.assertEquals(1, sampleEmbeddedObject.anInt);
            Assertions.assertTrue(sampleEmbeddedObject.aBoolean);
            Assertions.assertNull(sampleEmbeddedObject.aString);
        }
    }

}
