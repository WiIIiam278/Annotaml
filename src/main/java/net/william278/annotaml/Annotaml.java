package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

public class Annotaml<T> {

    @NotNull
    private final T object;

    @NotNull
    private final File file;

    protected Annotaml(@NotNull T yamlObject, @NotNull File outputFile) {
        this.object = yamlObject;
        this.file = outputFile;
    }

    public static <T> void create(@NotNull T yamlObject, @NotNull File outputFile) throws AnnotamlException {
        new Annotaml<T>(yamlObject, outputFile).serializeYaml();
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(@NotNull File file, @NotNull Class<T> classType) throws AnnotamlException {
        // Read the file as a string if it exits
        final Map<String, Object> nestedYamlMap;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            nestedYamlMap = new Yaml().load(inputStream);
        } catch (FileNotFoundException e) {
            throw new AnnotamlException("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new AnnotamlException("IOException reading file: " + e.getMessage());
        }
        if (Objects.isNull(nestedYamlMap)) {
            throw new AnnotamlException("Yaml file could not be read: " + file.getAbsolutePath());
        }

        // Flatten nested maps
        final Map<String, Object> yamlMap = new HashMap<>();
        nestedYamlMap.forEach((key, value) -> {
            if (value instanceof Map) {
                final Map<String, Object> map = (Map<String, Object>) value;
                map.forEach((mapKey, mapValue) -> {
                    final StringJoiner path = new StringJoiner(".");
                    path.add(key);
                    path.add(mapKey);
                    yamlMap.put(path.toString(), mapValue);
                });
            } else {
                yamlMap.put(key, value);
            }
        });

        // Validate that the object type constructor with zero arguments
        final Optional<Constructor<?>> constructors = Arrays.stream(classType.getConstructors()).filter(
                constructor -> constructor.getParameterCount() == 0).findFirst();
        if (constructors.isEmpty()) {
            throw new AnnotamlException("Class type must have a zero argument constructor: " + classType.getName());
        }

        // Instantiate an object of the class type
        try {
            final Constructor<?> constructor = constructors.get();
            final T object = (T) constructor.newInstance();

            // Match field names from yaml map and set values
            final Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                // Get the field name
                final String fieldName = field.getName();

                // Match in camelCase and snake_case
                final StringJoiner camelCase = new StringJoiner(".");
                camelCase.add(fieldName);
                final StringJoiner snakeCase = new StringJoiner(".");
                snakeCase.add(fieldName.replaceAll(Pattern.quote("_"), ""));

                // Get the value from the yaml map if valid
                final Optional<Object> valueCamelCase = Optional.ofNullable(yamlMap.get(camelCase.toString()));
                final Optional<Object> valueSnakeCase = Optional.ofNullable(yamlMap.get(snakeCase.toString()));
                if (valueCamelCase.isPresent()) {
                    field.setAccessible(true);
                    field.set(object, valueCamelCase.get());
                } else if (valueSnakeCase.isPresent()) {
                    field.setAccessible(true);
                    field.set(object, valueSnakeCase.get());
                }
            }

            return object;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes a {@code YamlFile} annotated object to a YAML file.
     *
     * @throws AnnotamlException If an error occurs during serialization
     */
    @SuppressWarnings("unchecked")
    public void serializeYaml() throws AnnotamlException {
        final Class<?> objectClass = object.getClass();

        // Validate that the object is annotated with @YamlFile
        if (!objectClass.isAnnotationPresent(YamlFile.class)) {
            throw new AnnotamlException("Could not serialize object; no @YamlFile annotation is present");
        }

        // Validate that the object has a constructor with zero arguments
        if (Arrays.stream(objectClass.getConstructors()).noneMatch(constructor -> constructor.getParameterCount() == 0)) {
            throw new AnnotamlException("Could not serialize object; no zero-argument constructor is present");
        }

        // Prepare output location
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new AnnotamlException("Could not create output directory");
            }
        }
        if (file.exists()) {
            if (!file.delete()) {
                throw new AnnotamlException("Could not delete existing output file in order to overwrite");
            }
        }

        // Get file settings
        final boolean convertToSnakeCase = objectClass.getAnnotation(YamlFile.class).convertToSnakeCase();
        final String header = objectClass.getAnnotation(YamlFile.class).header();

        // Iterate through each field in the class and serialize it
        final Map<String, Object> keyValueMap = new HashMap<>();
        for (final Field field : objectClass.getDeclaredFields()) {
            try {
                // Validate the field
                field.setAccessible(true);
                if (field.isAnnotationPresent(IgnoredKey.class)) {
                    continue;
                }

                // Determine the keyed path to use for the field
                String fieldPath = field.getName();
                if (field.isAnnotationPresent(KeyPath.class)) {
                    fieldPath = field.getAnnotation(KeyPath.class).path();
                }

                // Convert field path names to snake case if necessary
                if (convertToSnakeCase) {
                    fieldPath = fieldPath.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
                }

                // If the field path name is period-separated, convert it to a nested map and add, otherwise add directly
                if (fieldPath.contains(".")) {
                    final String[] fieldPathParts = fieldPath.split("\\.");
                    Map<String, Object> currentMap = keyValueMap;
                    for (int i = 0; i < fieldPathParts.length - 1; i++) {
                        if (!currentMap.containsKey(fieldPathParts[i])) {
                            currentMap.put(fieldPathParts[i], new HashMap<>());
                        }
                        currentMap = (Map<String, Object>) currentMap.get(fieldPathParts[i]);
                    }
                    currentMap.put(fieldPathParts[fieldPathParts.length - 1], field.get(object));
                } else {
                    keyValueMap.put(fieldPath, field.get(object));
                }
            } catch (IllegalAccessException e) {
                throw new AnnotamlException("Cannot access field " + field.getName());
            }
        }

        // Write the document to a file
        try (final FileWriter writer = new FileWriter(file)) {
            // Write the header
            if (!header.isEmpty()) {
                writer.write("# " + header.trim().replaceAll(Pattern.quote("\n"), "\n# ") + "\n");
            }

            // Write the document
            getYaml().dump(keyValueMap, writer);
        } catch (Exception e) {
            throw new AnnotamlException("Could not write to output file");
        }
    }

    /**
     * Returns the configured Yaml for writing to files
     *
     * @return the yaml object
     */
    @NotNull
    private static Yaml getYaml() {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

}
