package io.jpower.sgf.ser;

/**
 * @author zheng.sun
 */
public class SerializationException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = -2000580286190120221L;

    public SerializationException() {
        super();
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public static SerializationException truncatedMessage() {
        return new SerializationException(
                "While parsing a protocol message, the input ended unexpectedly "
                        + "in the middle of a field.");
    }

    public static SerializationException negativeSize(int size) {
        return new SerializationException("Negative size: " + size);
    }

    public static SerializationException malformedVarint() {
        return new SerializationException("CodedReader encountered a malformed varint.");
    }

}
