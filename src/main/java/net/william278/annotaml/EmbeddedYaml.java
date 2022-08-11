package net.william278.annotaml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a type that can be serialized into an object within a YAML file
 * <p>
 * Not to be confused with {@link YamlFile}, which specifies custom types that can be embedded within this file.
 *
 * @implNote Requires a constructor with no arguments present.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EmbeddedYaml {
}
