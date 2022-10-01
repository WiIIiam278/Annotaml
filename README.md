# Annotaml
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)
[![](https://jitpack.io/v/net.william278/Annotaml.svg)](https://jitpack.io/#net.william278/Annotaml)

Annotaml is a library for generating YAML files from Java classes. It uses SnakeYAML for parsing and saving YAML files, providing an extremely easy and intuitive way of reading and writing yaml (configuration) files to objects through a set of Java annotations.

Annotate a configuration object to your liking and load/reload it to/from a YAML file with defaults, without having to bother working with reading and writing to/from keyed routes.

Requires Java 11+.

## Installation
Annotaml is available on JitPack. You can browse the Javadocs [here](https://javadoc.jitpack.io/net/william278/Annotaml/latest/javadoc/).

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
*WIP*
[Browse the Javadocs](https://javadoc.jitpack.io/net/william278/Annotaml/latest/javadoc/) for more methods and information.

## License
Annotaml is licensed under Apache-2.0.
