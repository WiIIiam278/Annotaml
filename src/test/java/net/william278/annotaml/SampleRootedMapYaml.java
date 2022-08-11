package net.william278.annotaml;

import java.util.Map;

@SuppressWarnings("unused")
@YamlFile(header = "Test for @RootedMap annotation")
public class SampleRootedMapYaml {

    // Test for root maps
    @RootedMap
    public Map<String, String> rootMap = Map.of("test", "hello", "test2", "hello there");

    public SampleRootedMapYaml() {
    }
}
