package com.neo.util.framework.api.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A ConfigNode
 */
public interface Config {

    /**
     * Returns the key of the config
     */
    String key();

    /**
     * Retrieves a subconfig based on the key
     *
     * @param key key
     *
     * @return subconfig
     */
    Config get(String key);

    /**
     * The config type of the current config
     */
    Config.Type type();

    /**
     * True if this config exists
     */
    default boolean exists() {
        return this.type().exists();
    }

    /**
     * True if this config is a leaf
     */
    default boolean isLeaf() {
        return this.type().isLeaf();
    }

    /**
     * True if this config has a designated value
     */
    boolean hasValue();

    /**
     * Returns the {@link ConfigValue} of this config
     *
     * @param clazz the class which the {@link ConfigValue} should be
     * @param <T> the class
     * @return the config value of the config
     */
    <T> ConfigValue<T> as(Class<T> clazz);

    /**
     * Config convert from String, you can use
     *
     * @param mapper method to create an instance from config
     * @param <T>    type
     * @return typed value
     */
    <T> ConfigValue<T> as(Function<Config, T> mapper);

    /**
     * The {@link ConfigValue} as a {@link Boolean}
     */
    default ConfigValue<Boolean> asBoolean() {
        return this.as(Boolean.class);
    }

    /**
     * The {@link ConfigValue} as a {@link String}
     */
    default ConfigValue<String> asString() {
        return this.as(String.class);
    }

    /**
     * The {@link ConfigValue} as a {@link Integer}
     */
    default ConfigValue<Integer> asInt() {
        return this.as(Integer.class);
    }

    /**
     * The {@link ConfigValue} as a {@link Long}
     */
    default ConfigValue<Long> asLong() {
        return this.as(Long.class);
    }

    /**
     * The {@link ConfigValue} as a {@link Double}
     */
    default ConfigValue<Double> asDouble() {
        return this.as(Double.class);
    }

    /**
     * The {@link ConfigValue} as a {@link List} of the provided class
     */
    <T> ConfigValue<List<T>> asList(Class<T> clazz);

    /**
     * The {@link ConfigValue} as a {@link Map}
     */
    ConfigValue<Map<String, String>> asMap();

    enum Type {
        OBJECT(true, false),
        LIST(true, false),
        VALUE(true, true),
        MISSING(false, false);

        private final boolean exists;
        private final boolean isLeaf;

        Type(boolean exists, boolean isLeaf) {
            this.exists = exists;
            this.isLeaf = isLeaf;
        }

        public boolean exists() {
            return this.exists;
        }

        public boolean isLeaf() {
            return this.isLeaf;
        }
    }

}
