package net.william278.annotaml;

/**
 * Indicates an exception occurred loading or saving a YAML file.
 */
public class AnnotamlException extends IllegalStateException {

    public AnnotamlException(String message) {
        super(message);
    }

}
