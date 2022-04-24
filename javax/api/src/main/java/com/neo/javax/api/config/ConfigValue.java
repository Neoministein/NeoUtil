package com.neo.javax.api.config;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ConfigValue<T> {

    String key();

    Optional<T> asOptional();

    T get();

    default boolean isPresent() {
        return this.asOptional().isPresent();
    }

    default void ifPresentOrElse(Consumer<T> action, Runnable emptyAction) {
        Optional<T> optional = this.asOptional();
        if (optional.isPresent()) {
            action.accept(optional.get());
        } else {
            emptyAction.run();
        }

    }

    default void ifPresent(Consumer<? super T> consumer) {
        this.asOptional().ifPresent(consumer);
    }

    default Optional<T> filter(Predicate<? super T> predicate) {
        return this.asOptional().filter(predicate);
    }

    default <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return this.asOptional().map(mapper);
    }

    default <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return this.asOptional().flatMap(mapper);
    }

    default T orElse(T other) {
        return this.asOptional().orElse(other);
    }

    default T orElseGet(Supplier<? extends T> other) {
        return this.asOptional().orElseGet(other);
    }

    default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return this.asOptional().orElseThrow(exceptionSupplier);
    }

    default Stream<T> stream() {
        return this.asOptional().stream();
    }
}
