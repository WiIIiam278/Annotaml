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
     */
    //todo
    @NotNull
    String versionField() default "version";

    /**
     * The current version number of the file. This will be written to the config to the field specified by {@link #versionField()}. If {@link #versionField()} is {@code ""}, this will be omitted from the config.
     * <p>
     * Default: {@code 1}
     *
     * @return The current version number of the file
     */
    //todo
    int versionNumber() default 1;

}