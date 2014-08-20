package edu.usc.enl.dynamicmeasurement.process;

/**
 * Created with IntelliJ IDEA.
 * User: masoud
 * Date: 8/4/13
 * Time: 10:29 AM
 */
public class ConfigParseException extends Exception {

    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigParseException(Throwable cause) {
        super(cause);
    }
}
