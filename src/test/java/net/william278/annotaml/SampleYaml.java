package net.william278.annotaml;

@SuppressWarnings("unused")
@YamlFile(header = "This is a header\nIt can go across\nMultiple lines")
public class SampleYaml {

    public String test = "hello";

    @KeyPath(value = "test_key_two")
    public String test2 = "hello there";

    @IgnoredKey
    public String test3 = "hello there again";

    @KeyPath(value = "integers.test_key_four")
    public int test4 = 1;

    @KeyPath(value = "integers.test_cinco")
    public int test5 = 5;

    @KeyPath(value = "integers.test_six")
    public TestEnum test6 = TestEnum.TEST_TWO;

    public String testWithWeirdCharacters = "Hello: This is a test with weird # characters!";

    public String snakeCaseConversionTest1 = "Snake case conversion test!";

    @KeyPath(value = "keyPath.snakeCaseConversion.testString")
    public String snakeCaseConversionTest2 = "Snake case conversion test 2!";

    public enum TestEnum {
        TEST_ONE,
        TEST_TWO,
        TEST_THREE
    }

    public SampleYaml() {
    }

}
