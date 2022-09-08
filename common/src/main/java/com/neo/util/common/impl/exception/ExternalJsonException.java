package com.neo.util.common.impl.exception;

/**
 * This class handles all json Exceptions associated with external input
 */
public class ExternalJsonException extends ExternalInputException {

    /**
     * Constructs a new ExternalJsonException with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ExternalJsonException() {
        super();
    }

    /**
     * Constructs a new ExternalJsonException with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public ExternalJsonException(String message) {
        super(message);
    }

    /**
     * Constructs a new ExternalJsonException with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public ExternalJsonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ExternalJsonException with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public ExternalJsonException(Throwable cause) {
        super(cause);
    }

    public ExternalJsonException(InternalJsonException ex) {
        this(ex.getMessage(), ex);
    }
}
