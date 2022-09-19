package net.william278.annotaml;

import java.util.Map;

@SuppressWarnings("unused")
@YamlFile
public class SampleKeyedMapField {

    @KeyPath("nested.test_path")
    public Map<String, String> keyedMap = Map.of("hello", "String one",
            "world", "String two");

    public SampleKeyedMapField() {
    }

}
