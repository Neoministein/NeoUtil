package com.neo.util.framework.api.config;

import com.neo.util.common.impl.exception.ConfigurationException;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A wrapper for a Key-Value set with additional features.
 * @param <T> the type of value
 */
public interface ConfigValue<T> {

    /**
     * The key associated to the config value
     */
    String key();

    /**
     * The value as an {@link Optional<T>}
     */
    Optional<T> asOptional();

    /**
     * The value but throws a {@link ConfigurationException} if not found
     */
    T get() throws ConfigurationException;

    /**
     * Set the value of the current config
     */
    void set(T value);

    /**
     * True if the value is present
     */
    default boolean isPresent() {
        return this.asOptional().isPresent();
    }

    /**
     * Runs specific action if present or not
     *
     * @param action to run if present
     * @param emptyAction to run if empty
     */
    default void ifPresentOrElse(Consumer<T> action, Runnable emptyAction) {
        Optional<T> optional = this.asOptional();
        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            emptyAction.run();
        }

    }

    /**
     * Runs the consumer if present
     */
    default void ifPresent(Consumer<? super T> consumer) {
        this.asOptional().ifPresent(consumer);
    }

    /**
     * @see Optional#filter(Predicate)
     */
    default Optional<T> filter(Predicate<? super T> predicate) {
        return this.asOptional().filter(predicate);
    }

    /**
     * @see Optional#map(Function)
     */
    default <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return this.asOptional().map(mapper);
    }

    /**
     * @see Optional#flatMap(Function)
     */
    default <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return this.asOptional().flatMap(mapper);
    }

    /**
     * @see Optional#orElse(Object)
     */
    default T orElse(T other) {
        return this.asOptional().orElse(other);
    }

    /**
     * @see Optional#orElseGet(Supplier)
     */
    default T orElseGet(Supplier<? extends T> other) {
        return this.asOptional().orElseGet(other);
    }

    /**
     * @see Optional#orElseThrow(Supplier)
     */
    default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return this.asOptional().orElseThrow(exceptionSupplier);
    }

    /**
     * @see Optional#stream()
     */
    default Stream<T> stream() {
        return this.asOptional().stream();
    }
}
