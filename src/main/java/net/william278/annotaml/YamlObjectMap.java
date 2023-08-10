/*
 * This file is part of Annotaml, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.annotaml;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.dejvokep.boostedyaml.utils.conversion.PrimitiveConversions.*;

/**
 * Represents a {@link T} object as a mapped set of paths to their object values, as read to/from a {@link YamlFile}
 *
 * @param <T> The type of object this map is representing
 */
@ApiStatus.Internal
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
    @ApiStatus.Internal
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
    @ApiStatus.Internal
    protected static <T> YamlObjectMap<T> parse(@NotNull T defaults, @NotNull InputStream yaml) throws
            IllegalArgumentException, IOException {
        return new YamlObjectMap<>(defaults).readFromYaml(YamlDocument.create(yaml));
    }

    /**
     * Read the map of field default paths to values from the object to this map
     *
     * @param object the object to read from
     */
    @SuppressWarnings("unchecked")
    private void readDefaults(@NotNull T object) throws IllegalArgumentException {
        // Validate object
        if (!object.getClass().isAnnotationPresent(YamlFile.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @YamlFile");
        }

        // Check if this is a rooted map, then begin iterating through the fields
        final boolean rootedMap = object.getClass().getAnnotation(YamlFile.class).rootedMap();
        final Field[] fields = object.getClass().getDeclaredFields();
        int fieldIndex = 0;
        for (final Field field : fields) {
            // Ensure the field is accessible
            field.setAccessible(true);

            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(YamlIgnored.class)) {
                continue;
            }

            // If the field is annotated with @YamlKey, use the value as the key
            final String key = rootedMap ? ""
                    : field.isAnnotationPresent(YamlKey.class)
                    ? field.getAnnotation(YamlKey.class).value()
                    : field.getName();

            // If the field is the first in the object, add the header as a comment
            if (fieldIndex == 0) {
                final String headerComment = getObjectClass().getAnnotation(YamlFile.class).header();
                if (!headerComment.isEmpty()) {
                    comments.put(key, headerComment);
                }
            }

            // If the field has a comment annotation, add it to the comments map
            if (field.isAnnotationPresent(YamlComment.class)) {
                if (comments.containsKey(key)) {
                    comments.put(key, comments.get(key) + "\n" + field.getAnnotation(YamlComment.class).value());
                } else {
                    comments.put(key, field.getAnnotation(YamlComment.class).value());
                }
            }

            // If it's a rooted map, add the read map values to the root of the map
            if (rootedMap) {
                try {
                    readFieldValue(field, object).ifPresent(value -> this.putAll((Map<String, Object>) value));
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Unable to read rooted map value " + field.getName(), e);
                }
                return;
            }

            // Attempt to read the value from the field and add it to the map
            try {
                final Optional<Object> value = readFieldValue(field, object);
                this.put(key, value.orElse(null));
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Unable to read field " + field.getName() + " from object " +
                        object.getClass().getName() + " to map at YAML path " + field.getName(), e);
            }

            fieldIndex++;
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
        final boolean rootedMap = defaults.getClass().getAnnotation(YamlFile.class).rootedMap();

        // Iterate through each field
        final Field[] fields = defaults.getClass().getDeclaredFields();
        for (final Field field : fields) {
            // Ensure the field is accessible
            field.setAccessible(true);

            // Ignore fields that are annotated with @YamlIgnored
            if (field.isAnnotationPresent(YamlIgnored.class)) {
                continue;
            }

            // Handle rooted maps
            if (rootedMap) {
                if (!field.getType().equals(Map.class)) {
                    throw new IllegalArgumentException("Field " + field.getName() + " is part of a rooted map but is not a Map (is "
                            + field.getType().getName() + ")");
                }

                // Write the rooted map to the field
                try {
                    writeFieldValue(field, defaults, this);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Unable to write rooted field " + field.getName() + " from object " +
                            defaults.getClass().getName() + " to " + field.getName(), e);
                }
                return defaults;
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <Y> void writeFieldValue(@NotNull Field field, @NotNull T object, @NotNull Y value)
            throws IllegalAccessException, IllegalArgumentException {
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
            final Map<String, ?> map = ((Section) value).getStringRouteMappedValues(false);
            settableObject = map;
            if (fieldClass == TreeMap.class) {
                settableObject = new TreeMap<>(map);
            } else if (fieldClass == LinkedHashMap.class) {
                settableObject = new LinkedHashMap<>(map);
            } else if (fieldClass == HashMap.class) {
                settableObject = new HashMap<>(map);
            } else if (fieldClass == ConcurrentHashMap.class) {
                settableObject = new ConcurrentHashMap<>(map);
            }
        }

        // Handle enums
        if (fieldClass.isEnum()) {
            try {
                settableObject = Enum.valueOf((Class<? extends Enum>) fieldClass, value.toString());
            } catch (IllegalArgumentException e) {
                // If the config entered enum wasn't found, try to match it
                for (final Enum<?> enumValue : ((Class<? extends Enum>) fieldClass).getEnumConstants()) {
                    if (enumValue.name().equalsIgnoreCase(value.toString())) {
                        settableObject = enumValue;
                        break;
                    }
                }
            }
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
        // Ensure the field is accessible
        field.setAccessible(true);

        // If the object is an enum, return the name of the enum
        if (field.getType().isEnum()) {
            return Optional.ofNullable(field.get(object)).map(Object::toString);
        }

        // Otherwise, return the value of the field
        return Optional.ofNullable(field.get(object));
    }

    /**
     * Read the map of field paths to values from the YAML document to this map
     *
     * @param yamlDocument the {@link YamlDocument} to read from
     */
    @NotNull
    private YamlObjectMap<T> readFromYaml(@NotNull YamlDocument yamlDocument) {
        // If it's a rooted map, read each value from the root
        if (getObjectClass().getAnnotation(YamlFile.class).rootedMap()) {
            this.clear();
            this.putAll(yamlDocument.getStringRouteMappedValues(false));
            return this;
        }

        // Otherwise, read each field from the mapped document
        this.forEach((key, value) -> this.put(key, yamlDocument.get(key)));
        return this;
    }

    /**
     * Write the map of field paths to values to disk
     *
     * @param file The file to write to
     * @throws IOException If the file could not be written to
     */
    @ApiStatus.Internal
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

        // Set key-values and associated comments if applicable
        this.forEach((key, value) -> {
            // Set the value
            yamlDocument.set(key, value);

            // Set block comments
            if (comments.containsKey(key)) {
                yamlDocument.getBlock(key).setComments((Arrays
                        .stream(comments.get(key).split("\\r?\\n"))
                        .map(String::trim)
                        .map(comment -> " " + comment)
                        .collect(Collectors.toList())));
            }
        });

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
    @ApiStatus.Internal
    @NotNull
    protected T getObject() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return this.applyMapTo(Annotaml.getDefaults(objectClass));
    }

    /**
     * Get the object class type represented by this map
     *
     * @return The object represented by this map
     */
    @ApiStatus.Internal
    @NotNull
    protected Class<T> getObjectClass() {
        return this.objectClass;
    }

}
