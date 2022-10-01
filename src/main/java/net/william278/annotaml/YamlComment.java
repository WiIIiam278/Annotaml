package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a comment to be placed within a {@link YamlFile} above the key path node.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface YamlComment {

    /**
     * The comment to include in the generated file
     *
     * @return The comment to include in the generated file
     */
    @NotNull
    String value();

}
