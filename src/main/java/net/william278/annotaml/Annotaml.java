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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a YAML file that can be parsed and dumped to/from disk
 *
 * @param <T> The type of object this YAML file represents
 */
@SuppressWarnings("unused")
public class Annotaml<T> {

    /**
     * <b>Internal</b> - The object this Annotaml instance is representing
     */
    @NotNull
    private final Class<T> objectClass;

    /**
     * <b>Internal</b> - {@link YamlObjectMap} of the read object
     */
    @NotNull
    private final YamlObjectMap<T> yamlObjectMap;

    /**
     * <b>Internal</b> - Generate a new {@link Annotaml} instance from an object
     *
     * @param object The object to generate the {@link Annotaml} instance from, to be mapped into a {@link YamlObjectMap}
     * @throws IllegalArgumentException If the object is not annotated with {@link YamlFile}
     */
    @SuppressWarnings("unchecked")
    private Annotaml(@NotNull T object) throws IllegalArgumentException {
        if (!object.getClass().isAnnotationPresent(YamlFile.class)) {
            throw new IllegalArgumentException("Object type must be annotated with @YamlFile");
        }
        this.objectClass = (Class<T>) object.getClass();
        this.yamlObjectMap = new YamlObjectMap<>(object);
    }

    /**
     * <b>Internal</b> - Instantiate a new {@link Annotaml} instance from a {@link YamlObjectMap}
     *
     * @param yamlObjectMap The {@link YamlObjectMap} to instantiate the {@link Annotaml} instance from
     */
    private Annotaml(@NotNull YamlObjectMap<T> yamlObjectMap) {
        this.yamlObjectMap = yamlObjectMap;
        this.objectClass = yamlObjectMap.getObjectClass();
    }

    /**
     * Generate a new {@link Annotaml} of a {@link T objectClass} from a {@link File}
     * <p>
     * If the file does not exist, it will be created using the defaults translated from a new instantiation of the default object.
     *
     * @param file        The file to read the object from
     * @param objectClass The class object to instantiate
     * @param <T>         The type of object this YAML file represents
     * @return A new {@link Annotaml} instance
     * @throws IOException               If the file cannot be read
     * @throws IllegalArgumentException  If the object is not annotated with {@link YamlFile}
     * @throws IllegalAccessException    If the object cannot be instantiated
     * @throws InstantiationException    If the object cannot be instantiated
     * @throws InvocationTargetException If the object cannot be instantiated
     */
    @NotNull
    public static <T> Annotaml<T> create(@NotNull File file, @NotNull Class<T> objectClass) throws IOException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        return create(file, getDefaults(objectClass));
    }

    /**
     * Generate a new {@link Annotaml} of a {@link T object} from a {@link File}, using the object field values as defaults
     * <p>
     * If the file does not exist, it will be created using the defaults translated from the default object.
     *
     * @param file   The file to read the object from
     * @param object The default values of the file
     * @param <T>    The type of object this YAML file represents
     * @return A new {@link Annotaml} instance
     * @throws IOException If the file cannot be read
     */
    @NotNull
    public static <T> Annotaml<T> create(@NotNull File file, @NotNull T object) throws IOException {
        final Annotaml<T> annotaml = file.exists() ? create(object, new FileInputStream(file)) : create(object);
        if (!file.exists()) {
            annotaml.save(file);
        }
        return annotaml;
    }

    /**
     * Create a new {@link Annotaml} instance from a {@link YamlFile}-annotated object
     * <p>
     * Note the object must have a zero-argument constructor.
     *
     * @param object The object to create the {@link Annotaml} instance from
     * @param <T>    The type of the object. Must be annotated with {@link YamlFile}
     * @return A new {@link Annotaml} instance
     */
    @NotNull
    public static <T> Annotaml<T> create(@NotNull T object) {
        return new Annotaml<>(object);
    }

    /**
     * Parse a {@link Annotaml} of a {@link T object} from a {@link InputStream} of YAML
     *
     * @param defaults    Default values to use if the YAML does not contain a value for a key
     * @param inputStream The {@link InputStream} of the yaml file to read from
     * @param <T>         The type of the object to parse
     * @return A {@link Annotaml} of the parsed object
     * @throws IOException If an error occurs while reading the YAML
     */
    @NotNull
    public static <T> Annotaml<T> create(@NotNull T defaults, @NotNull InputStream inputStream) throws IOException {
        return new Annotaml<>(YamlObjectMap.parse(defaults, inputStream));
    }

    /**
     * Create a new {@link Annotaml} by reading an {@link InputStream} to a {@link YamlFile}-annotated object.
     * <p>
     * Note the object must have a zero-argument constructor. Default values from the object will be used if the YAML
     * does not contain a value for a field key.
     *
     * @param objectClass The class of the object to read
     * @param inputStream The {@link InputStream} of the yaml file to read from
     * @param <T>         The type of the object to read
     * @return A new {@link Annotaml} instance
     * @throws IOException               If an error occurs while reading the {@link InputStream}
     * @throws InvocationTargetException If an error occurs while invoking the constructor of the object
     * @throws InstantiationException    If an error occurs while instantiating the object
     * @throws IllegalAccessException    If an error occurs while accessing the object
     */
    @NotNull
    public static <T> Annotaml<T> create(@NotNull Class<T> objectClass, @NotNull InputStream inputStream) throws IOException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        return new Annotaml<>(YamlObjectMap.parse(Annotaml.getDefaults(objectClass), inputStream));
    }

    /**
     * Save the dumped field keyed field values of the object to a YAML file
     *
     * @param file The file to save the object to
     * @throws IllegalArgumentException If the object is not annotated with {@link YamlFile}
     * @throws IOException              If an error occurs while writing to the file
     */
    public void save(@NotNull File file) throws IllegalArgumentException, IOException {
        yamlObjectMap.save(file);
    }

    /**
     * Get the object represented by this {@link Annotaml} instance
     *
     * @return The object represented by this {@link Annotaml} instance
     * @throws InvocationTargetException If an error occurs while invoking the object constructor
     * @throws InstantiationException    If an error occurs while instantiating the object
     * @throws IllegalAccessException    If an error occurs while accessing the object constructor
     */
    @NotNull
    public T get() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return yamlObjectMap.getObject();
    }

    /**
     * Instantiate a new object of the type to get the defaults
     *
     * @param objectClass The class of the object to get the defaults for
     * @param <T>         The type of the object to get the defaults for
     * @return A new instance of the object with the defaults
     * @throws InvocationTargetException If the constructor throws an exception
     * @throws InstantiationException    If the class that declares the underlying constructor represents an abstract class
     * @throws IllegalAccessException    If the underlying constructor is inaccessible
     * @throws IllegalArgumentException  If the object does not contain a zero-argument constructor
     */
    protected static <T> T getDefaults(@NotNull Class<T> objectClass) throws InvocationTargetException,
            InstantiationException, IllegalAccessException, IllegalArgumentException {
        // Validate that the object type constructor with zero arguments
        final Optional<Constructor<?>> constructors = Arrays.stream(objectClass.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0).findFirst();
        if (constructors.isEmpty()) {
            throw new IllegalArgumentException("Class type must have a zero-argument constructor: " + objectClass.getName());
        }

        // Get the constructor
        final Constructor<?> constructor = constructors.get();
        constructor.setAccessible(true);

        // Instantiate an object of the class type to act as the base
        @SuppressWarnings("unchecked") final T defaults = (T) constructor.newInstance();
        return defaults;
    }

}
