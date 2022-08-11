package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a file that can be saved and loaded from a YAML file. Requires a constructor with no arguments present.
 * <p>
 * Not to be confused with {@link EmbeddedYaml}, which specifies custom types that can be embedded within this file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface YamlFile {

    /**
     * Whether to convert fields to {@code snake_case}
     * <p>
     * Default: {@code true}
     *
     * @return Whether to convert fields to {@code snake_case}
     */
    boolean convertToSnakeCase() default true;

    /**
     * Header to include at the top of the generated file
     *
     * @return Header to include at the top of the generated file
     */
    @NotNull
    String header() default "";

    /**
     * The field to use as a key for the version of the file
     * <p>
     * If set to {@code ""} (empty string), the version will be omitted from the file
     * <p>
     * Default: {@code ""}
     *
     * @return The field to use as a key for the version of the file
     */
    @NotNull
    String versionField() default "";

    /**
     * The current version number of the file. This will be written to the config to the field specified by {@link #versionField()}. If {@link #versionField()} is {@code ""}, this will be omitted from the config.
     * <p>
     * Default: {@code 1}
     *
     * @return The current version number of the file
     */
    int versionNumber() default 1;

}
