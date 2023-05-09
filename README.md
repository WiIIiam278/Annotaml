# Annotaml
[![Discord](https://img.shields.io/discord/818135932103557162?color=7289da&logo=discord)](https://discord.gg/tVYhJfyDWG)
[![Maven](https://repo.william278.net/api/badge/latest/releases/net/william278/annotaml?color=00fb9a&name=Maven&prefix=v)

**Annotaml** is a library for generating YAML files from Java classes. It uses SnakeYAML for parsing and saving YAML files, providing an extremely easy and intuitive way of reading and writing yaml (configuration) files to objects through a set of Java annotations.

Annotate a configuration object to your liking and load/reload it to/from a YAML file with defaults, without having to bother working with reading and writing to/from keyed routes.

Requires Java 11+.

## Installation
Annotaml is available [on Maven](https://repo.william278.net/#/releases/net/william278/annotaml/). You can browse the Javadocs [here](https://repo.william278.net/javadoc/releases/net/william278/annotaml/latest).

### Gradle
<details>
<summary>Gradle setup instructions</summary> 

First, add the Maven repository to your `build.gradle` file.:
```groovy
repositories {
    maven { url "https://repo.william278.net/releases" }
}
```

Then, add the dependency itself. Replace `VERSION` with the latest release version. (e.g. `2.0.2`). If you want to target pre-release "snapshot" versions (not reccommended), you should use the `/snapshots` repository instead.
```groovy
dependencies {
    implementation "net.william278:annotaml:VERSION"
}
```
</details>

### Maven & others
JitPack has a [handy guide](https://jitpack.io/#net.william278/Annotaml/#How_to) for how to use the dependency with other build platforms.

## Usage
*WIP*
[Browse the Javadocs](https://repo.william278.net/javadoc/releases/net/william278/annotaml/latest) for more methods and information.

## License
Annotaml is licensed under Apache-2.0.
