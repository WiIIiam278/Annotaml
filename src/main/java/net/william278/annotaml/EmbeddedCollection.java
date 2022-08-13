package net.william278.annotaml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a key contains embedded {@code Map} or {@code List} values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EmbeddedCollection {

    /**
     * The type of the list.
     *
     * @return The type of the list.
     */
    Class<?> value();

}
