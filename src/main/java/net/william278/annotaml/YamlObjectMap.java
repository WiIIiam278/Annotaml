package net.william278.annotaml;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions.NON_NUMERIC_CONVERSIONS;
import static dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions.convertNumber;

/**
 * Represents a {@link T} object as a mapped set of paths to their object values, as read to/from a {@link YamlFile}
 *
 * @param <T> The type of object this map is representing
 */
public class YamlObjectMap<T> extends LinkedHashMap<String, Object> {

    /**
     * <b>Internal</b> - The object this YamlObjectMap instance is representing
     */
    @NotNull
    private final Class<T> objectClass;

    @NotNull
    private final Map<String, String> comments;

    /**
     * Create a new YamlObjectMap from an object
     *
     * @param object The object to create a YamlObjectMap from
     * @throws IllegalArgumentException If the object is not annotated with {@link YamlFile}
     */
    @SuppressWarnings("unchecked")
    protected YamlObjectMap(@NotNull T object) throws IllegalArgumentException {
        super();

        // Validate that the @YamlFile annotation is present
        if (!object.getClass().isAnnotationPresent(YamlFile.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @YamlFile");
        }

        // Read the object to the map
        this.objectClass = (Class<T>) object.getClass();
        this.comments = new LinkedHashMap<>();
        this.readDefaults(object);
    }

    /**
     * Parse a {@link YamlObjectMap} of a {@link T object} from a {@link InputStream} of YAML
     *
     * @param defaults Default values to use if the YAML does not contain a value for a key
     * @param yaml     The YAML to parse
     * @param <T>      The type of the object to parse
     * @return A {@link YamlObjectMap} of the parsed object
     * @throws IllegalArgumentException If the object type is not annotated with {@link YamlFile}
     * @throws IOException              If an error occurs while reading the YAML
     */
    protected static <T> YamlObjectMap<T> parse(@NotNull T defaults, @NotNull InputStream yaml) throws
            IllegalArgumentException, IOException {
        return new YamlObjectMap<>(defaults).readFromYaml(YamlDocument.create(yaml));
    }

    /**
     * Read the map of field default paths to values from the object to this map
     *
     * @param object the object to read from
     */
    private void readDefaults(@NotNull T object) throws IllegalArgumentException {
        // Iterate through each field
        final Field[] fields = object.getClass().getDeclaredFields();
        for (final Field field : fields) {
            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(YamlIgnored.class)) {
                continue;
            }

            // If the field is annotated with @YamlKey, use the value as the key
            final String key = field.isAnnotationPresent(YamlKey.class) ?
                    field.getAnnotation(YamlKey.class).value() : field.getName();

            // If the field has a comment annotation, add it to the comments map
            if (field.isAnnotationPresent(YamlComment.class)) {
                comments.put(key, field.getAnnotation(YamlComment.class).value());
            }

            // Attempt to read the value from the field and add it to the map
            try {
                final Optional<Object> value = readFieldValue(field, object);
                this.put(key, value.orElse(null));
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to read field " + field.getName() + " from object " +
                                                   object.getClass().getName() + " to map at YAML path " + field.getName(), e);
            }
        }
    }

    /**
     * Set the fields of a {@link T object} from the values in this map
     *
     * @param defaults The object to set the fields of
     * @return The object with the fields set
     * @throws IllegalArgumentException If a field could not be accessed and set from the map
     */
    private T applyMapTo(@NotNull T defaults) throws IllegalArgumentException {
        // Iterate through each field
        final Field[] fields = defaults.getClass().getDeclaredFields();
        for (final Field field : fields) {
            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(YamlIgnored.class)) {
                continue;
            }

            // If the field is annotated with @YamlKey, use the value as the key
            final String key = field.isAnnotationPresent(YamlKey.class) ?
                    field.getAnnotation(YamlKey.class).value() : field.getName();
            Optional.ofNullable(this.get(key)).ifPresent(value -> {
                try {
                    writeFieldValue(field, defaults, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Unable to write field " + field.getName() + " from object " +
                                                       defaults.getClass().getName() + " to YAML path " + field.getName(), e);
                }
            });

        }
        return defaults;
    }

    /**
     * Read a field value from an object
     *
     * @param field  The field to read
     * @param object The object to read the field from
     * @param value  The value to set the field to
     * @throws IllegalAccessException If the field could not be accessed
     */
    @SuppressWarnings("unchecked")
    private <Y> void writeFieldValue(@NotNull Field field, @NotNull T object, @NotNull Y value) throws
            IllegalAccessException, IllegalArgumentException {
        // Set the field to be accessible
        field.setAccessible(true);

        // Convert the value safely
        final Class<?> fieldClass = field.getType();
        Object settableObject = fieldClass.isInstance(value) ? value
                : PrimitiveConversions.isNumber(value.getClass()) && PrimitiveConversions.isNumber(fieldClass)
                ? (Y) convertNumber(value, fieldClass)
                : NON_NUMERIC_CONVERSIONS.containsKey(value.getClass()) && NON_NUMERIC_CONVERSIONS.containsKey(fieldClass)
                ? value
                : null;

        // Handle maps
        if (value instanceof Section) {
            settableObject = ((Section) value).getStringRouteMappedValues(false);
        }

        // Set the field value
        if (settableObject != null) {
            field.set(object, settableObject);
        } else {
            throw new IllegalArgumentException("Unable to set field " + field.getName() + " of type " +
                                               fieldClass.getName() + " to value " + value);
        }
    }

    /**
     * Read the value of a field from an object
     *
     * @param field  The field to read
     * @param object The object to read the field from
     * @return The value of the field, wrapped within an Optional. If the field is null, the Optional will be empty.
     * @throws IllegalAccessException If the field is inaccessible and could not be read for any reason
     */
    private Optional<Object> readFieldValue(@NotNull Field field, @NotNull T object) throws IllegalAccessException {
        field.setAccessible(true);
        return Optional.ofNullable(field.get(object));
    }

    /**
     * Read the map of field paths to values from the YAML document to this map
     *
     * @param yamlDocument the {@link YamlDocument} to read from
     */
    @NotNull
    private YamlObjectMap<T> readFromYaml(@NotNull YamlDocument yamlDocument) {
        this.forEach((key, value) -> this.put(key, yamlDocument.get(key)));
        return this;
    }

    /**
     * Write the map of field paths to values to disk
     *
     * @param file The file to write to
     */
    public void save(@NotNull File file) throws IOException {
        // Create parent directories
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Unable to create parent directories for file " + file.getAbsolutePath());
            }
        }

        // Create file
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Unable to create file " + file.getAbsolutePath());
            }
        }

        // Create YamlDocument that will be dumped as a file
        final YamlDocument yamlDocument = YamlDocument.create(file);

        // Add the header comment
        final String headerComment = getObjectClass().getAnnotation(YamlFile.class).header();
        if (!headerComment.isBlank()) {
            yamlDocument.setComments(Arrays
                    .stream(headerComment.split("\n"))
                    .map(String::trim)
                    .map(comment -> " " + comment)
                    .collect(Collectors.toList()));
        }

        // Set key-values and associated comments if applicable
        this.forEach((key, value) -> {
            yamlDocument.set(key, value);
            if (comments.containsKey(key)) {
                yamlDocument.getBlock(key).setComments((Arrays
                        .stream(comments.get(key).split("\n"))
                        .map(String::trim)
                        .map(comment -> " " + comment)
                        .collect(Collectors.toList())));
            }
        });
        //todo fix comment duplication
        yamlDocument.save();
    }

    /**
     * Get the object represented by the map, by applying the read map to a newly instantiated {@link T object}
     *
     * @return The {@link T object}
     * @throws InvocationTargetException If the object could not be invoked during instantiation
     * @throws InstantiationException    If the object could not be instantiated
     * @throws IllegalAccessException    If the object could not be instantiated
     */
    @NotNull
    protected T getObject() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.applyMapTo(Annotaml.getDefaults(objectClass));
    }

    /**
     * Get the object class type represented by this map
     *
     * @return The object represented by this map
     */
    @NotNull
    protected Class<T> getObjectClass() {
        return this.objectClass;
    }

}