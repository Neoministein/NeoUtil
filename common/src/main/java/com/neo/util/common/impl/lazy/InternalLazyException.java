package com.neo.util.common.impl.lazy;

import com.neo.util.common.impl.exception.InternalLogicException;
/**
 * This class handles all Exceptions associated which can occur during lazy action
 */
public class InternalLazyException extends InternalLogicException {
    /**
     * Constructs a new InternalLazyException with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public InternalLazyException() {
        super();
    }

    /**
     * Constructs a new InternalLazyException with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public InternalLazyException(String message) {
        super(message);
    }

    /**
     * Constructs a new InternalLazyException with the specified detail message and
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
    public InternalLazyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InternalLazyException with the specified cause and a detail
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
    public InternalLazyException(Throwable cause) {
        super(cause);
    }
}
