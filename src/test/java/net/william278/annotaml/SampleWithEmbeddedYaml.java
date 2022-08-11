package net.william278.annotaml;

import java.util.List;

@SuppressWarnings("unused")
@YamlFile(header = "Test for embedded objects in a yaml")
public class SampleWithEmbeddedYaml {

    public SampleEmbeddedObject embeddedObject = new SampleEmbeddedObject();

    public List<SampleEmbeddedObject> embeddedObjectList = List.of(new SampleEmbeddedObject(), new SampleEmbeddedObject());

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
