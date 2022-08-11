package net.william278.annotaml;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface YamlFile {

    /**
     * Whether to convert fields to {@code snake_case}
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

}
