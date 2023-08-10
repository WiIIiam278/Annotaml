# Annotaml
[![Build Status](https://github.com/WiIIiam278/Annotaml/actions/workflows/ci.yml/badge.svg)](https://github.com/WiIIiam278/Annotaml/actions/workflows/ci.yml)
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)
[![Maven](https://repo.william278.net/api/badge/latest/releases/net/william278/annotaml?color=00fb9a&name=Maven&prefix=v)](https://repo.william278.net/#/releases/net/william278/annotaml/)

**Annotaml** is a library for reading/writing YAML files to/from Java (11+) classes using annotations&mdash;handy for generating simple config files on-the-fly. It's not-so-great great for complex configuration structures, but good enough for [my projects](https://william278.net/).

Internally, this is built atop [dejvokep's boosted-yaml](https://github.com/dejvokep/boosted-yaml); itself built atop SnakeYaml.

## Setup
Annotaml is available [on Maven](https://repo.william278.net/#/releases/net/william278/annotaml/). You can browse the Javadocs [here](https://repo.william278.net/javadoc/releases/net/william278/annotaml/latest).

<details>
<summary>Gradle setup instructions</summary> 

First, add the Maven repository to your `build.gradle` file:
```groovy
repositories {
    maven { url "https://repo.william278.net/releases" }
}
```

Then, add the dependency itself. Replace `VERSION` with the latest release version. (e.g. `2.0.5`). If you want to target pre-release "snapshot" versions (not recommended), you should use the `/snapshots` repository instead.

```groovy
dependencies {
    implementation "net.william278:annotaml:VERSION"
}
```
</details>

Using Maven/something else? There's instructions on how to include Annotaml on [the repo browser](https://repo.william278.net/#/releases/net/william278/annotaml).

## Usage

Here's the basic user's manual for working with Annotaml. [Browse the Javadocs](https://repo.william278.net/javadoc/releases/net/william278/annotaml/latest) for all the methods and options at your disposal.

### Creating a config class

Annotaml is designed to make creating a config as thoughtless as possible. First, you'll need to create a config class as such and annotate it with `@YamlConfig`. Note your class must have a zero-args constructor to facilitate instantiation during reflection:

<details>
<summary>Example: MyConfig class</summary>

```java
import net.william278.annotaml.annotations.*;

@YamlConfig
public class MyConfig {

    // Annotaml supports most basic data structures, but *does not support custom classes for the sake of simplicity*.
    public String myString = "Hello, world!";
    public int myInt = 123;

    // You can also use arrays, lists, & maps.
    public ArrayList<String> myList = List.of("Hello", "world!");

    // Members don't have to be public.
    private String privateString = "Private members are supported too!";

    // Annotate with @YamlIgnore to prevent a member from being read/written to the YAML file.
    @YamlIgnore
    public String ignoredString = "This string will not be read/written to the YAML file.";

    // Annotate with @YamlKey to change the key used for a member in the YAML file. Use periods (.) to nest keys.
    @YamlKey("custom_key_name")
    public String customKeyName = "This string will be read/written to the YAML file with the key custom_key_name.";

    // You can add comments to the YAML file to help document it a bit better. They'll be put above the member.
    @YamlComment("This is a comment above the member.")
    public String commentExample = "This string will be read/written to the YAML file with a comment above it.";

    // You *must* include a zero-args constructor.
    private MyConfig() {
    }
}
```

</details>

This will generate a file that looks like this:

<details>
<summary>Example: Output config</summary>

```yaml
myString: Hello, world!
myInt: 123
myList:
  - Hello
  - world!
privateString: Private members are supported too!
custom_key_name: This string will be read/written to the YAML file with the key custom_key_name.
# This is a comment above the member.
commentExample: This string will be read/written to the YAML file with a comment above it.
```
</details>

You can also specify a header to put at the top of the file by setting the "header" annotation parameter of `@YamlFile`:

<details>
<summary>Example: Config headers</summary>

```java
@YamlFile(header = "This is a header!")
public class MyConfig {
    // ...
}
```

Which will add the following to the top of the file:

```yaml
# This is a header!
```
</details>

Calling `new MyConfig()` will now provide us with an instance of the MyConfig object with the default values. We can now use this to read/write YAML files.

### The Annotaml wrapper
The Annotaml class provides a wrapper over `@YamlFile`-annotated objects.

<details>
<summary>Example: Fetch config or create default if it doesn't exist</summary>

```java
public class AppClass {
    
    private MyConfig config;

    /**
     * Load the config file, or create a default config file using the defaults if one is not already present on disk.
     * @throws IllegalStateException if the config fails to load
     */
    public void createConfig() throws IllegalStateException {
        try {
            // Create an annotaml instance. This will read the config file from disk, or create a new one if it doesn't exist.
            final Annotaml<MyConfig> annotaml = Annotaml.create(new File("./config.yml"), new MyConfig());
            
            // Then, get the config object being wrapped by Annotaml.
            this.config = annotaml.get();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to create config", e);
        }
    }

}
```
</details>

You can also just pass the class type which will automatically instantiate the defaults for you:

<details>
<summary>Example: Automatic instantiation</summary>

```java
public class AppClass {

    private MyConfig config;

    public void createConfig() throws IllegalStateException {
        try {
            final Annotaml<MyConfig> annotaml = Annotaml.create(new File("./config.yml"), MyConfig.class).get();
        } catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to create config", e);
        }
    }

}
```
</details>

### Updating configs
Let's say you update the value of your config during the lifecycle of your app. To save the config, create a new Annotaml instance and call `save()`:

<details>
<summary>Example: Saving config</summary>

```java
public class AppClass {

    private MyConfig config;

    public void saveConfig() throws IllegalStateException {
        this.config.myString = "Save me!";
        
        try {
            // Create a *new* Annotaml wrapper over the newly modified object...
            final Annotaml<MyConfig> annotaml = Annotaml.create(config);
            
            // Call #save() on it to write the config to disk!
            annotaml.save(new File("./saved_config.yml")); // This will overwrite the file if it already exists.
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to save config", e);
        }
    }

}
```
</details>

### Further examples
Have a look at the [unit tests](https://github.com/WiIIiam278/Annotaml/tree/master/src/test), which demonstrate (and test) Annotaml's various functionality.

## License
Annotaml is licensed under Apache-2.0. See [LICENSE](https://github.com/WiIIiam278/Annotaml/raw/master/LICENSE) for more information.