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
