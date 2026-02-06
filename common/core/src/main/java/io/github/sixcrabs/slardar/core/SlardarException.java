package io.github.sixcrabs.slardar.core;

/**
 * <p>
 * .
 * </p>
 *
 * @author Alex
 * @version v1.0 2023/3/14
 */
public class SlardarException extends Exception {


    public SlardarException() {
    }

    public SlardarException(String message) {
        super(message);
    }

    public SlardarException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlardarException(Throwable cause) {
        super(cause);
    }

    public SlardarException(String msgTpl, Object... args) {
        super(String.format(msgTpl, args));
    }
}