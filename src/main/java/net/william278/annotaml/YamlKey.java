package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a value within a {@link YamlFile} that can be parsed and dumped to/from disk.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface YamlKey {

    /**
     * Path to use for the key, overriding the default path (field name)
     *
     * @return Path to use for the key
     */
    @NotNull
    String value();

}
