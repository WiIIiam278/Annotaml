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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

/**
 * Identifies a file that can be parsed and dumped as a {@link YamlDocument}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface YamlFile {

    /**
     * Header to include at the top of the generated file
     *
     * @return Header to include at the top of the generated file
     */
    @NotNull
    String header() default "";

    /**
     * Indicates if this file should be read/written to/from a single map field
     *
     * @return If this file is a rooted map
     */
    boolean rootedMap() default false;

    /**
     * The field to use as a key for the version of the file
     * <p>
     * If set to {@code ""} (empty string), the version will be omitted from the file
     * <p>
     * Default: {@code ""}
     *
     * @return The field to use as a key for the version of the file
     * @deprecated
     */
    @Deprecated(since = "2.0.2")
    @NotNull
    String versionField() default "version";

    /**
     * The current version number of the file. This will be written to the config to the field specified by {@link #versionField()}. If {@link #versionField()} is {@code ""}, this will be omitted from the config.
     * <p>
     * Default: {@code 1}
     *
     * @return The current version number of the file
     * @deprecated
     */
    @Deprecated(since = "2.0.2")
    int versionNumber() default 1;

}