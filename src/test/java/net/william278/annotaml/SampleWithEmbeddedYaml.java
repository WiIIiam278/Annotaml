package net.william278.annotaml;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@YamlFile(header = "Test for embedded objects in a yaml")
public class SampleWithEmbeddedYaml {

    public SampleEmbeddedObject embeddedObject = new SampleEmbeddedObject();

    @EmbeddedCollection(SampleEmbeddedObject.class)
    public List<SampleEmbeddedObject> embeddedObjectList = List.of(new SampleEmbeddedObject(), new SampleEmbeddedObject());

    @EmbeddedCollection(SampleEmbeddedObject.class)
    public Map<String, SampleEmbeddedObject> embeddedObjectMap = Map.of("key1", new SampleEmbeddedObject(), "key2", new SampleEmbeddedObject());

    public SampleWithEmbeddedYaml() {
    }

    @EmbeddedYaml
    public static class SampleEmbeddedObject {

        public int anInt = 1;
        public String aString = "aString";
        public boolean aBoolean = true;
        public double aDouble = 0.0d;
        public float aFloat = 1.0f;
        public long aLong = 1;

        public SampleEmbeddedObject() {
        }

    }
}
