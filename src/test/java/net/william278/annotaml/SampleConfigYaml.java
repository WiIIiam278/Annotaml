package net.william278.annotaml;

import java.util.Map;

@SuppressWarnings("unused")
@YamlFile(header = "Example YAML for testing a config file", versionField = "version")
public class SampleConfigYaml {

    @IgnoredKey
    public String ignoredField = "This field is ignored";

    // Tests for various data types
    public String stringField = "Hello";
    public boolean booleanField = true;
    public int intField = 1;
    public long longField = 1;
    public float floatField = 1.0f;


    public String[] stringArrayField = new String[] {"Hello", "World"};
    public boolean[] booleanArrayField = new boolean[] {true, false, true};
    public int[] intArrayField = new int[] {1, 2, 3};
    public long[] longArrayField = new long[] {1, 2, 3};
    public float[] floatArrayField = new float[] {1.0f, 2.0f, 3.0f};
    public double[] doubleArrayField = new double[] {1.0, 2.0, 3.0};

    public Map<String, String> mapField = Map.of("test", "hello", "test2", "hello there");
    public ConfigEnum enumField = ConfigEnum.TEST_TWO;
    public ConfigEnum[] enumArrayField = new ConfigEnum[] {ConfigEnum.TEST_ONE, ConfigEnum.TEST_TWO, ConfigEnum.TEST_THREE};

    public SampleConfigYaml() {
    }

    public enum ConfigEnum {
        TEST_ONE,
        TEST_TWO,
        TEST_THREE
    }
}
