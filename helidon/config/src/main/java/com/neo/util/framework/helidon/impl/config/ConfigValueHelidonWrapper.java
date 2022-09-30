package com.neo.util.framework.helidon.impl.config;

import com.neo.util.common.impl.exception.InternalLogicException;
import com.neo.util.framework.api.config.ConfigValue;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Wraps the hellion specific config implementation onto a generic one {@link ConfigValue}
 * @param <T>
 */
public class ConfigValueHelidonWrapper<T> implements ConfigValue<T> {

    protected io.helidon.config.ConfigValue<T> configValue;

    public  ConfigValueHelidonWrapper(io.helidon.config.ConfigValue<T> configValue) {
        this.configValue = configValue;
    }

    @Override
    public String key() {
        return configValue.name();
    }

    @Override
    public Optional<T> asOptional() {
        return configValue.asOptional();
    }

    @Override
    public T get() {
        return configValue.get();
    }

    @Override
    public void set(T value) {
        throw new InternalLogicException("Saving config is not supported in the Helidon implementation");
    }

    @Override
    public boolean isPresent() {
        return configValue.isPresent();
    }

    @Override
    public void ifPresentOrElse(Consumer<T> action, Runnable emptyAction) {
        configValue.ifPresentOrElse(action, emptyAction);
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
        configValue.ifPresent(consumer);
    }

    @Override
    public Optional<T> filter(Predicate<? super T> predicate) {
        return configValue.filter(predicate);
    }

    @Override
    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return configValue.map(mapper);
    }

    @Override
    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return configValue.flatMap(mapper);
    }

    @Override
    public T orElse(T other) {
        return configValue.orElse(other);
    }

    @Override
    public T orElseGet(Supplier<? extends T> other) {
        return configValue.orElseGet(other);
    }

    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return configValue.orElseThrow(exceptionSupplier);
    }

    @Override
    public Stream<T> stream() {
        return configValue.stream();
    }
}
