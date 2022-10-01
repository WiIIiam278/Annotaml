package net.william278.annotaml;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@YamlFile(header = "Hello, world! \nTest header comment!\nThird line")
public class TestYamlFile {

    public String test = "test";
    public int test2 = 2;
    @YamlKey("test3.nested.test")
    public boolean test3 = true;
    @YamlComment("Test comment")
    public double test4 = 4.0;
    @YamlKey("test_5")
    public float test5 = 5.0f;
    @YamlIgnored
    public String ignored = "ignored";
    public Map<TestEnum, String> test6 = Map.of(TestEnum.TEST, "test",
            TestEnum.TEST2, "test2",
            TestEnum.TEST3, "test3");
    public List<String> list = List.of("test", "test2", "test3");

    public TestYamlFile() {
    }

    public enum TestEnum {
        TEST,
        TEST2,
        TEST3
    }
}
