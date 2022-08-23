package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Annotaml is a library for generating YAML files from Java classes.
 *
 * @param <T> The type of the class to generate the YAML file for.
 */
@SuppressWarnings("unused")
public class Annotaml<T> {

    /**
     * The class to generate the YAML file for.
     */
    @NotNull
    private final T object;

    /**
     * The file to generate the YAML file for.
     */
    @NotNull
    private final File file;

    /**
     * Create a new Annotaml instance.
     *
     * @param yamlObject The class to generate the YAML file for.
     * @param outputFile The file to generate the YAML file for.
     */
    protected Annotaml(@NotNull T yamlObject, @NotNull File outputFile) {
        this.object = yamlObject;
        this.file = outputFile;
    }


    /**
     * Reloads a YAML file from a {@link File} into a {@link T} typed object, doing a few things:
     * <ol>
     *     <li>Checks if the file exists; if not, writes the file and returns the defaults</li>
     *     <li>If the file exists, copy and write defaults over if {@code copyDefaults} is true</li>
     *     <li>Read the file from disk, parse the YAML into the object and return it</li>
     * </ol>
     *
     * @param file     The target file that will be reloaded.
     * @param defaults The object with defaults to use if not set/if the file doesn't exist.
     * @param options  {@link LoaderOptions} to use for (re)loading the file.
     * @param <T>      the type of the class to load to/from the {@link File}
     * @return the YAML-parsed contents of the {@link File} on disk
     * @throws AnnotamlException If an error occurs saving or loading the object to/from YAML
     */
    @SuppressWarnings("unchecked")
    public static <T> T reload(@NotNull File file, @NotNull T defaults, final LoaderOptions options) throws AnnotamlException {
        // If the file doesn't exist, write the defaults
        if (!file.exists()) {
            save(defaults, file);
            return defaults;
        }

        // Read the existing file on disk
        try (InputStream inputStream = new FileInputStream(file)) {
            final T loadedFile = (T) load(inputStream, defaults.getClass());

            // Copy the defaults over if requested
            if (options.isCopyDefaults()) {
                copyDefaults(defaults, loadedFile);
                save(loadedFile, file);
            }

            // Read the file from disk and parse it into the object
            return loadedFile;
        } catch (FileNotFoundException e) {
            throw new AnnotamlException("YAML File does not exist: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new AnnotamlException("Error reading file: " + file.getAbsolutePath());
        }
    }

    /**
     * Reloads a YAML file from a {@link File} into a {@link T} typed object, doing a few things:
     * <ol>
     *     <li>Loads the defaults from the provided resource InputStream</li>
     *     <li>Checks if the file exists; if not, writes the file and returns the defaults</li>
     *     <li>If the file exists, copy and write defaults over if {@code copyDefaults} is true</li>
     *     <li>Read the file from disk, parse the YAML into the object and return it</li>
     * </ol>
     *
     * @param file                The target file that will be reloaded.
     * @param defaultsInputStream The InputStream of a yaml document to read default values from.
     * @param classType           The type of the class to load to/from the {@link File}
     * @param options             {@link LoaderOptions} to use for (re)loading the file.
     * @param <T>                 the type of the class to load to/from the {@link File}
     * @return the YAML-parsed contents of the {@link File} on disk
     * @throws AnnotamlException If an error occurs saving or loading the object to/from YAML
     */
    public static <T> T reload(@NotNull File file, @NotNull InputStream defaultsInputStream,
                               @NotNull Class<T> classType, @NotNull LoaderOptions options) throws AnnotamlException {
        return reload(file, load(defaultsInputStream, classType), options);
    }

    /**
     * Write a {@link YamlFile} annotated object to a YAML file.
     *
     * @param yamlObject The object to save.
     * @param outputFile The file to save to.
     * @param <T>        The type of the object to save.
     * @throws AnnotamlException If an error occurs saving the YAML file.
     */
    public static <T> void save(@NotNull T yamlObject, @NotNull File outputFile) throws AnnotamlException {
        new Annotaml<T>(yamlObject, outputFile).writeYaml();
    }

    /**
     * Loads a YAML file into an object. The class to deserialize must be a {@code YamlFile} annotated object and have a zero-argument constructor
     *
     * @param file      The file to load
     * @param classType The class to load the file into
     * @param <T>       The type of the object to load into
     * @return The object loaded from the file
     * @throws AnnotamlException If there is an error loading the file
     */
    public static <T> T load(@NotNull File file, @NotNull Class<T> classType) throws AnnotamlException {
        try (final InputStream inputStream = new FileInputStream(file)) {
            return load(inputStream, classType);
        } catch (FileNotFoundException e) {
            throw new AnnotamlException("YAML File does not exist: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new AnnotamlException("Error reading file: " + file.getAbsolutePath());
        }
    }

    /**
     * Loads a YAML file into an object. The class to deserialize must be a {@code YamlFile} annotated object and have a zero-argument constructor
     *
     * @param inputStream InputStream of the file to load
     * @param classType   The class to load the file into
     * @param <T>         The type of the object to load into
     * @return The object loaded from the file
     * @throws AnnotamlException If there is an error loading the file
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(@NotNull InputStream inputStream, @NotNull Class<T> classType) throws AnnotamlException {
        // Validate the classType is an annotated YamlFile
        if (!classType.isAnnotationPresent(YamlFile.class)) {
            throw new AnnotamlException("Class type must be annotated with @YamlFile: " + classType.getName());
        }

        // Validate that the object type constructor with zero arguments
        final Optional<Constructor<?>> constructors = Arrays.stream(classType.getConstructors()).filter(
                constructor -> constructor.getParameterCount() == 0).findFirst();
        if (constructors.isEmpty()) {
            throw new AnnotamlException("Class type must have a zero argument constructor: " + classType.getName());
        }

        // Read the file as a string to a flat yaml map if it exits
        final Map<String, Object> yamlMap = readFlattenedYamlMap(inputStream, classType);

        // Instantiate an object of the class type
        try {
            final Constructor<?> constructor = constructors.get();
            final T object = (T) constructor.newInstance();

            // Match field names from yaml map and set values
            final Field[] fields = classType.getDeclaredFields();
            for (final Field field : fields) {
                // Skip fields that are annotated with @IgnoredKey
                if (field.isAnnotationPresent(IgnoredKey.class)) {
                    continue;
                }

                // Set the rooted map to all values
                if (field.isAnnotationPresent(RootedMap.class)) {
                    // If the field is a map, set the value to the map else throw an error
                    if (field.getType().equals(Map.class)) {
                        field.set(object, yamlMap);
                    } else {
                        throw new AnnotamlException("Field " + field.getName() + " is a RootedMap but is not present in the YAML");
                    }
                    continue;
                }

                // Get the field name
                String fieldPath = getKeyedFieldName(field, yamlMap);

                // If the field type is an embedded object, read it from the child nodes
                if (field.getType().isAnnotationPresent(EmbeddedYaml.class)) {
                    // Filter yamlMap by objects that start with the fieldPath and a period and remove the fieldPath from the keys
                    final Map<String, Object> childMap = yamlMap.entrySet().stream().filter(
                            entry -> entry.getKey().startsWith(fieldPath + ".")).collect(Collectors.toMap(
                            entry -> entry.getKey().substring(fieldPath.length() + 1), Map.Entry::getValue));
                    field.set(object, readEmbeddedObject(field.getType(), childMap));
                    continue;
                }

                // If the field contains a collection of embedded objects
                if (field.isAnnotationPresent(EmbeddedCollection.class)) {
                    if (List.class.equals(field.getType())) {
                        final Class<?> listType = field.getAnnotation(EmbeddedCollection.class).value();
                        final List<Object> readList = new ArrayList<>();
                        for (final Object listItem : (List<?>) yamlMap.get(fieldPath)) {
                            final Map<String, Object> itemValues = ((Map<String, Object>) listItem)
                                    .entrySet().stream().flatMap(Annotaml::flatten)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            readList.add(readEmbeddedObject(listType, itemValues));
                        }
                        field.set(object, readList);
                        continue;
                    }

                    // If the field contains a map of embedded objects
                    if (Map.class.equals(field.getType()) && field.isAnnotationPresent(EmbeddedCollection.class)) {
                        final Class<?> mapType = field.getAnnotation(EmbeddedCollection.class).value();

                        // Get all the keys that start with the field path and remove the field path
                        final Set<String> fieldPathKeys = yamlMap.keySet().stream().filter(key -> key.startsWith(fieldPath))
                                .map(key -> key.substring(fieldPath.length() + 1)).collect(Collectors.toSet());
                        final Map<String, Object> embeddedObjectKeys = new LinkedHashMap<>();
                        fieldPathKeys.forEach(key -> {
                            final String mapKey = key.split("\\.")[0];
                            final Object mapObject = yamlMap.get(fieldPath + "." + key);
                            if (!embeddedObjectKeys.containsKey(mapKey)) {
                                embeddedObjectKeys.put(mapKey, new LinkedHashMap<>());
                            }
                            ((Map<String, Object>) embeddedObjectKeys.get(mapKey)).put(key.split("\\.")[1], mapObject);
                        });

                        // Iterate through unread values and read each value as an embedded object then add to the map
                        final Map<String, Object> readMap = new LinkedHashMap<>();
                        embeddedObjectKeys.forEach((key, value) -> {
                            final Map<String, Object> itemValues = ((Map<String, Object>) value)
                                    .entrySet().stream().flatMap(Annotaml::flatten)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            readMap.put(key, readEmbeddedObject(mapType, itemValues));
                        });

                        field.set(object, readMap);
                        continue;
                    }

                    throw new AnnotamlException("@EmbeddedCollection field must be a List or Map: " + field.getType().getName());
                }

                // Set the field value if present in the yaml map
                if (yamlMap.containsKey(fieldPath)
                    && yamlMap.get(fieldPath) != null) {

                    getSettableValue(field, yamlMap.get(fieldPath)).ifPresent(settable -> {
                        try {
                            field.set(object, settable);
                        } catch (IllegalAccessException e) {
                            throw new AnnotamlException("Error setting field value: " + field.getName());
                        }
                    });
                } else {
                    // Set the field value to null (uninitialized)
                    if (!field.getType().isPrimitive()) {
                        field.set(object, null);
                    }
                }
            }

            return object;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new AnnotamlException("Error instantiating class: " + classType.getName());
        }
    }

    /**
     * Reads an embedded object from a YAML map
     * <p>
     * The object type must be annotated with {@code EmbeddedYaml} and have a zero-argument constructor
     *
     * @param classType The class to read the object into
     * @param values    The values to instantiate the object with from the map
     * @param <T>       The type of the object to create from the map
     * @return A new object instantiated and set from the map of values
     * @throws AnnotamlException If there is an error reading the embedded object
     */
    private static <T> Object readEmbeddedObject(@NotNull Class<T> classType, @NotNull Map<String, Object> values) throws AnnotamlException {
        try {
            // Instantiate a new object of the embedded type
            final Constructor<?> embeddedConstructor = classType.getConstructor();
            final Object embeddedObject = embeddedConstructor.newInstance();

            // Iterate through embedded fields
            Arrays.stream(embeddedObject.getClass().getDeclaredFields()).forEach(embeddedField -> {
                // Get the field name
                String embeddedFieldPath = getKeyedFieldName(embeddedField, values);

                // Set the value of the embedded field
                embeddedField.setAccessible(true);
                if (values.containsKey(embeddedFieldPath)
                    && values.get(embeddedFieldPath) != null
                    && !embeddedField.isAnnotationPresent(IgnoredKey.class)) {

                    final Object value = values.get(embeddedFieldPath);

                    getSettableValue(embeddedField, value).ifPresent(settable -> {
                        try {
                            embeddedField.set(embeddedObject, settable);
                        } catch (IllegalAccessException e) {
                            throw new AnnotamlException("Error setting field value: " + classType.getName());
                        }
                    });
                } else {
                    try {
                        if (!embeddedField.getType().isPrimitive()) {
                            embeddedField.set(embeddedObject, null);
                        }
                    } catch (IllegalAccessException e) {
                        throw new AnnotamlException("Error setting unset or ignored field value to null: " + classType.getName());
                    }
                }
            });

            return embeddedObject;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new AnnotamlException("Error instantiating embedded object: " + classType.getName());
        }
    }

    /**
     * Returns the name of a field as formatted in a key of mapped yaml
     *
     * @param field  The field to get the name of
     * @param values The yaml map to determine the name with
     * @return The name of the field
     */
    @NotNull
    private static String getKeyedFieldName(@NotNull Field field, @NotNull Map<String, Object> values) {
        String fieldName = field.getName();

        // If the field is annotated with KeyPath, set field name to the path
        if (field.isAnnotationPresent(KeyPath.class)) {
            fieldName = field.getAnnotation(KeyPath.class).value();
        }

        // Convert to snake case if necessary
        if (!values.containsKey(fieldName)) {
            fieldName = convertToSnakeCase(fieldName);
        }

        return fieldName;
    }

    /**
     * Get the version number of the YAML file.
     *
     * @param file      The file to get the version number of.
     * @param classType The type of the class to get the version number of.
     * @param <T>       The type of the class to get the version number of.
     * @return The version number of the YAML file.
     * @throws AnnotamlException If an error occurs reading the YAML file.
     */
    public static <T> Optional<Integer> getVersionNumber(@NotNull File file, @NotNull Class<T> classType) throws AnnotamlException {
        try (final InputStream inputStream = new FileInputStream(file)) {
            return getVersionNumber(inputStream, classType);
        } catch (FileNotFoundException e) {
            throw new AnnotamlException("YAML File does not exist: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new AnnotamlException("Error reading file: " + file.getAbsolutePath());
        }
    }

    /**
     * Get the version number of the YAML file.
     *
     * @param inputStream InputStream of the file to get the version number of.
     * @param classType   The type of the class to get the version number of.
     * @param <T>         The type of the class to get the version number of.
     * @return The version number of the YAML file.
     * @throws AnnotamlException If an error occurs reading the YAML file.
     */
    public static <T> Optional<Integer> getVersionNumber(@NotNull InputStream inputStream, @NotNull Class<T> classType) throws AnnotamlException {
        // Validate the classType is an annotated YamlFile
        if (!classType.isAnnotationPresent(YamlFile.class)) {
            throw new AnnotamlException("Class type must be annotated with @YamlFile: " + classType.getName());
        }

        // Read the file as a string to a flat yaml map if it exits
        final Map<String, Object> yamlMap = readFlattenedYamlMap(inputStream, classType);

        // Get the version number from the YAML file
        final String versionField = classType.getAnnotation(YamlFile.class).versionField();
        if (versionField.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable((Integer) yamlMap.get(versionField));
        } catch (ClassCastException e) {
            throw new AnnotamlException("Invalid version number in config file");
        }
    }

    /**
     * Copies the defaults from the {@code defaults} object to the {@code file} where they are missing
     *
     * @param defaultFile The default file to copy from
     * @param targetFile  The actual loaded file to copy to
     * @param <T>         The type of the object to copy the defaults from and to
     */
    private static <T> void copyDefaults(@NotNull T defaultFile, @NotNull T targetFile) throws AnnotamlException {
        // Validate both files are annotated with YamlFile
        if (!defaultFile.getClass().isAnnotationPresent(YamlFile.class)) {
            throw new AnnotamlException("Default file must be annotated with YamlFile");
        }
        if (!targetFile.getClass().isAnnotationPresent(YamlFile.class)) {
            throw new AnnotamlException("Loaded file must be annotated with YamlFile");
        }

        // Iterate through each field in the default file
        final Field[] fields = defaultFile.getClass().getDeclaredFields();
        for (final Field defaultField : fields) {
            try {
                // If the default field is null, skip it
                if (defaultField.get(defaultFile) == null) {
                    continue;
                }

                // Get the corresponding field by name in the loaded file
                final Field loadedField = targetFile.getClass().getDeclaredField(defaultField.getName());

                // If the loaded field is null, set the loaded field to the default field
                if (loadedField.get(targetFile) == null) {
                    loadedField.set(targetFile, defaultField.get(defaultFile));
                }
            } catch (IllegalAccessException | NoSuchFieldException e1) {
                throw new AnnotamlException("Could not copy default value to loaded file: " + e1.getMessage());
            }
        }
    }

    /**
     * Reads a file to a YAML map and flattens nested maps to a single level.
     *
     * @param inputStream File stream to read
     * @param classType   The object type to read to.
     * @param <T>         The type of the object to read to.
     * @return A flattened map representing the read YAML data
     * @throws AnnotamlException If an error occurs reading the file, or if it was not found.
     */
    private static <T> Map<String, Object> readFlattenedYamlMap(@NotNull InputStream inputStream, @NotNull Class<T> classType) throws AnnotamlException {
        final Map<String, Object> nestedYamlMap;
        nestedYamlMap = getYaml(getEmbeddedClassTypes(classType)).load(inputStream);
        if (Objects.isNull(nestedYamlMap)) {
            throw new AnnotamlException("Failed to read YAML file");
        }

        // Flatten nested maps to period separated keys
        return nestedYamlMap.entrySet().stream().flatMap(Annotaml::flatten)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Serializes and writes {@link YamlFile} annotated object to a YAML file.
     *
     * @throws AnnotamlException If an error occurs during serialization
     */
    @SuppressWarnings("unchecked")
    protected void writeYaml() throws AnnotamlException {
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

        // Get file settings
        final boolean convertToSnakeCase = objectClass.getAnnotation(YamlFile.class).convertToSnakeCase();
        final String header = objectClass.getAnnotation(YamlFile.class).header();
        final String versionField = objectClass.getAnnotation(YamlFile.class).versionField();
        final int versionNumber = objectClass.getAnnotation(YamlFile.class).versionNumber();

        // Serialize rootedFields
        final Map<String, Object> keyValueMap = new LinkedHashMap<>();

        // Find rootedFields with @RootedMap
        final List<Field> rootedFields = Arrays.stream(objectClass.getDeclaredFields()).filter(field -> field.isAnnotationPresent(RootedMap.class)).collect(Collectors.toList());
        if (rootedFields.size() > 1) {
            throw new AnnotamlException("Only one field can be annotated with @RootedMap");
        }
        rootedFields.stream().findFirst().ifPresent(rootedField -> {
            rootedField.setAccessible(true);
            try {
                final Object value = rootedField.get(object);
                if (value != null) {
                    keyValueMap.putAll(((Map<String, Object>) value));
                }
            } catch (IllegalAccessException e) {
                throw new AnnotamlException("Cannot access rooted map field " + rootedField.getName());
            }
        });

        // Iterate through other non-rooted field in the class and serialize it
        Arrays.stream(objectClass.getDeclaredFields()).filter(field -> !field.isAnnotationPresent(RootedMap.class)).forEach(field -> {
            try {
                // Validate the field
                field.setAccessible(true);
                if (field.isAnnotationPresent(IgnoredKey.class)) {
                    return;
                }

                // Determine the keyed path to use for the field
                String fieldPath = field.getName();

                // Convert field path names to snake case if necessary
                if (convertToSnakeCase) {
                    fieldPath = convertToSnakeCase(fieldPath);
                }

                // Or, if the field is annotated with KeyPath, use the annotated path
                if (field.isAnnotationPresent(KeyPath.class)) {
                    fieldPath = field.getAnnotation(KeyPath.class).value();
                    if (fieldPath.isEmpty()) {
                        throw new AnnotamlException("Could not serialize object; @KeyPath annotation is empty. Use @RootedKey to serialize a map root.");
                    }
                }

                // If the field is an enum, use the enum name
                Object fieldValue = field.get(object);
                if (field.getType().isEnum()) {
                    fieldValue = ((Enum<?>) field.get(object)).name();
                }

                // If the field is an array of enums, use the enum names
                if (field.getType().isArray()) {
                    if (field.getType().getComponentType().isEnum()) {
                        fieldValue = Arrays.stream((Object[]) field.get(object)).map(value -> ((Enum<?>) value).name()).collect(Collectors.toList());
                    }
                }

                // If the field path name is period-separated, convert it to a nested map first, otherwise add directly
                if (fieldPath.contains(".")) {
                    final String[] fieldPathParts = fieldPath.split("\\.");
                    Map<String, Object> currentMap = keyValueMap;
                    for (int i = 0; i < fieldPathParts.length - 1; i++) {
                        if (!currentMap.containsKey(fieldPathParts[i])) {
                            currentMap.put(fieldPathParts[i], new LinkedHashMap<>());
                        }
                        currentMap = (Map<String, Object>) currentMap.get(fieldPathParts[i]);
                    }
                    currentMap.put(fieldPathParts[fieldPathParts.length - 1], fieldValue);
                } else {
                    keyValueMap.put(fieldPath, fieldValue);
                }
            } catch (IllegalAccessException e) {
                throw new AnnotamlException("Cannot access field " + field.getName());
            }
        });

        // Add the version field if needed
        if (!versionField.isEmpty()) {
            if (keyValueMap.containsKey(versionField)) {
                throw new AnnotamlException("Could not serialize object; version field " + versionField + " already exists");
            }
            keyValueMap.put(versionField, versionNumber);
        }

        // Write the document to a file
        try (final FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, false)) {
            // Write the header
            if (!header.isEmpty()) {
                writer.write("# " + header.trim().replaceAll(Pattern.quote("\n"), "\n# ") + "\n");
            }

            // Write the document
            getYaml(getEmbeddedClassTypes(object)).dump(keyValueMap, writer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AnnotamlException("Could not write to output file");
        }
    }

    /**
     * Returns an array of embedded classes in the object.
     *
     * @param object The object to search for embedded classes
     * @param <T>    The type of the object
     * @return A list of embedded classes in the object
     */
    @NotNull
    private static <T> Class<?>[] getEmbeddedClassTypes(T object) {
        return Arrays.stream(object.getClass().getDeclaredFields()).map(Field::getType)
                .filter(type -> type.isAnnotationPresent(EmbeddedYaml.class) || type.isEnum())
                .filter(type -> !type.isAnnotationPresent(IgnoredKey.class))
                .toArray(Class<?>[]::new);
    }

    /**
     * Returns the configured Yaml for writing to files
     *
     * @return the yaml object
     */
    @NotNull
    private static Yaml getYaml(@NotNull Class<?>... extraClasses) {
        final DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setPrettyFlow(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        final Representer representer = new Representer();
        Arrays.stream(extraClasses).forEach(extraClass -> {
            // Add class tag
            representer.addClassTag(extraClass, Tag.MAP);

            // Add type description
            final TypeDescription typeDescription = new TypeDescription(extraClass);
            typeDescription.setExcludes(Arrays.stream(extraClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(IgnoredKey.class))
                    .map(Field::getName).collect(Collectors.toList()).toArray(String[]::new));
            representer.addTypeDescription(typeDescription);
        });

        return new Yaml(representer, options);
    }

    /**
     * Recursively flattens a nested map
     *
     * @param entry The map entry to flatten
     * @return The flattened map
     */
    @SuppressWarnings("unchecked")
    private static Stream<Map.Entry<String, Object>> flatten(@NotNull Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof Map<?, ?>) {
            Map<String, Object> nested = (Map<String, Object>) entry.getValue();

            return nested.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(entry.getKey() + "." + e.getKey(), e.getValue()))
                    .flatMap(Annotaml::flatten);
        }
        return Stream.of(entry);
    }

    /**
     * Converts a string to snake_case
     *
     * @param value the string to convert
     * @return the converted string
     */
    @NotNull
    protected static String convertToSnakeCase(@NotNull String value) {
        return value.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    /**
     * Gets the field settable value of an object
     *
     * @param field the field to set the value to
     * @param value the value to set
     * @return the object parsed to be settable if possible; empty otherwise
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Optional<Object> getSettableValue(@NotNull Field field, @NotNull Object value) {
        // Handle if directly settable
        if (field.getType().isAssignableFrom(value.getClass())) {
            return Optional.of(value);
        }

        // Handle enums
        if (field.getType().isEnum()) {
            try {
                return Optional.of(Enum.valueOf((Class<Enum>) field.getType(), value.toString().toUpperCase()));
            } catch (IllegalArgumentException | NullPointerException e) {
                return Optional.empty();
            }
        }

        // Handle arrays
        if (field.getType().isArray()) {
            // Create a new array of the field type
            final Object array = Array.newInstance(field.getType().getComponentType(), ((List) value).size());
            // Set the array values
            for (int i = 0; i < ((List) value).size(); i++) {
                try {
                    Array.set(array, i, ((List) value).get(i));
                } catch (IllegalArgumentException ie) {
                    try {
                        // Handle float arrays
                        Array.set(array, i, Float.parseFloat(((List) value).get(i).toString()));
                    } catch (IllegalArgumentException ie2) {
                        return Optional.empty();
                    }
                }
            }
            return Optional.of(array);
        }

        // Handle other objects and primitives
        final Optional<Object> parsedObject = parseObjectType(field, value);
        if (parsedObject.isPresent()) {
            return parsedObject;
        }

        throw new AnnotamlException("Value could not be parsed to field type: " + field.getType().getName());
    }

    /**
     * Parses an object to a primitive field-settable type
     *
     * @param field the field to set the value to
     * @param value the value to set
     * @return the object parsed to be settable if possible; empty otherwise
     */
    private static Optional<Object> parseObjectType(@NotNull Field field, @NotNull Object value) {
        if (boolean.class.equals(field.getType())) {
            return Optional.of(Boolean.parseBoolean(value.toString()));
        }
        if (int.class.equals(field.getType())) {
            return Optional.of(Integer.parseInt(value.toString()));
        }
        if (long.class.equals(field.getType())) {
            return Optional.of(Long.parseLong(value.toString()));
        }
        if (byte.class.equals(field.getType())) {
            return Optional.of(Byte.parseByte(value.toString()));
        }
        if (double.class.equals(field.getType())) {
            return Optional.of(Double.parseDouble(value.toString()));
        }
        if (float.class.equals(field.getType())) {
            return Optional.of(Float.parseFloat(value.toString()));
        }
        return Optional.empty();
    }

    /**
     * Provides options for serializing and deserializing {@code YamlFile} annotated objects.
     */
    public static class LoaderOptions {

        /**
         * Whether to copy default values from the class to the object
         */
        private boolean copyDefaults = true;


        private LoaderOptions() {
        }

        /**
         * Creates a new {@code LoaderOptions} object with the specified settings.
         *
         * @return the new {@code LoaderOptions} object
         */
        public static LoaderOptions builder() {
            return new LoaderOptions();
        }

        /**
         * Sets whether to copy default values from the class to the object
         *
         * @param copyDefaults whether to copy default values from the class to the object
         *                     <p>
         *                     Default: {@code true}
         * @return the {@code LoaderOptions} object
         */
        public LoaderOptions copyDefaults(boolean copyDefaults) {
            this.copyDefaults = copyDefaults;
            return this;
        }

        /**
         * Gets whether to copy default values from the class to the object
         *
         * @return whether to copy default values from the class to the object
         */
        public boolean isCopyDefaults() {
            return copyDefaults;
        }

    }

}
