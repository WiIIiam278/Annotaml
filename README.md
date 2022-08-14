# Annotaml
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)
[![](https://jitpack.io/v/net.william278/Annotaml.svg)](https://jitpack.io/#net.william278/Annotaml)

Annotaml is a library for generating YAML files from Java classes. It uses SnakeYAML for parsing and saving YAML files, providing an extremely easy and intuitive way of reading and writing yaml (configuration) files to objects through a set of Java annotations.

Annotate a configuration object to your liking and load/reload it to/from a YAML file with defaults, without having to bother working with reading and writing to/from keyed routes.

Requires Java 11+.

## Installation
Annotaml is available on JitPack.

### Maven
To use the library on Maven, in your `pom.xml` file, first add the JitPack repository:
```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

Then, add the dependency in your `<dependencies>` section. Remember to replace `Tag` with the current Annotaml version.
```xml
    <dependency>
        <groupId>net.william278</groupId>
        <artifactId>Annotaml</artifactId>
        <version>Tag</version>
        <scope>compile</scope>
    </dependency>
```

### Gradle & others
JitPack has a [handy guide](https://jitpack.io/#net.william278/Annotaml/#How_to) for how to use the dependency with other build platforms.

## Usage
Annotaml will convert `@YamlFile` annotated classes to and from YAML files. Objects require a zero-argument constructor present and the YamlFile annotation. Fields should be public.

Example of an annotated object:
```java
@YamlFile(header = "Example config file commented header!", 
        versionField = "version", // This will insert a "version" field in the config file for versioning.
        versionNumber = 1) // This will set the version number to 1. Fetch with Annotaml#getVersionNumber(...)
public class Settings {
    
    // Properties will be set in the config file based on their field names converted to camel case (conversion can be disabled)
    public String serverHost = "localhost"; // Default values you assign will be set by default
    public int serverPort = 18;
    
    @KeyPath("server_temperature") // This overrides the default key path of "server_degrees".
    public float serverDegrees = 20.0f;
    
    @IgnoredKey // Keys marked as ignored will not be included or read from the config file.
    public boolean loaded = false;
    
    public ArrayList<String> serverNames = new ArrayList<>(); // List<> and Map<> collections are supported
    
    public EmbeddedObject embeddedObject = new EmbeddedObject(); // Custom objects are supported!
    
    // Lists and string-object maps of embedded objects are fine, too, but you must annotate with
    // @EmbeddedCollection and provide the class type (due to array type erasure in Java)
    @EmbeddedCollection(EmbeddedObject.class)
    public HashMap<String, EmbeddedObject> embeddedObjectList = new ArrayList<>();

    public Settings() {
        // A Default zero-args constructor is required!
    }
    
    @EmbeddedYaml // Custom embedded object classes must be annotated with @EmbeddedYaml
    public static class EmbeddedObject {
        public String name = "";
        public int age = 0;
        
        public EmbeddedObject() {
            // A Default zero-args constructor is required!
        }
    }
}
```

Example (re)loading the settings (including copying defaults) from a config.yml file
```java
public class ConfigLoader() {

    // (re)-load the settings from disk
    public Settings loadSettings(File configFolder) {
        return Annotaml.reload(new File(configFolder, "config.yml"), // The config file to load to/from
                new Settings(), // Create a new settings object to use as defaults
                LoaderOptions.builder().copyDefaults(true)); // Set the loader to copy defaults
    }
    
}
```

## License
Annotaml is licensed under Apache-2.0.