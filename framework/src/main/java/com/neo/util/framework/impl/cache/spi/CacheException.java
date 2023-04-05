package com.neo.util.framework.impl.cache.spi;

/**
 * This exception is thrown when a cache value computation fails because of an exception. The cause of the failure can be
 * retrieved using the {@link Throwable#getCause()} method.
 */
public class CacheException extends RuntimeException {

    public CacheException(Throwable cause) {
        super(cause);
    }

    public CacheException(String message, Exception cause) {
        super(message, cause);
    }
}
