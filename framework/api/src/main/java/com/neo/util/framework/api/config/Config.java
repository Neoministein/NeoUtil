package com.neo.util.framework.api.config;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface Config {

    /**
     * Returns the key of the config
     * @return the key of the config
     */
    String key();

    /**
     * Retrieves a subconfig based on the key
     *
     * @param key key
     * @return subconfig
     */
    Config get(String key);

    /**
     * The config type of the current config
     * @return config
     */
    Config.Type type();

    /**
     * Checks if this config exists
     *
     * @return true if it exists
     */
    default boolean exists() {
        return this.type().exists();
    }

    /**
     * Checks if this config is a leaf
     *
     * @return true if it is a leaf
     */
    default boolean isLeaf() {
        return this.type().isLeaf();
    }

    /**
     * Checks if this config has a designated value
     *
     * @return true if it has a designated value
     */
    boolean hasValue();

    /**
     * Returns the {@link ConfigValue} of this config
     *
     * @param clazz the class which the {@link ConfigValue} should be
     * @param <T> the lass
     * @return the config value of the config
     */
    <T> ConfigValue<T> as(Class<T> clazz);

    <T> ConfigValue<T> as(Function<Config, T> var1);

    default ConfigValue<Boolean> asBoolean() {
        return this.as(Boolean.class);
    }

    default ConfigValue<String> asString() {
        return this.as(String.class);
    }

    default ConfigValue<Integer> asInt() {
        return this.as(Integer.class);
    }

    default ConfigValue<Long> asLong() {
        return this.as(Long.class);
    }

    default ConfigValue<Double> asDouble() {
        return this.as(Double.class);
    }

    <T> ConfigValue<List<T>> asList(Class<T> var1);

    ConfigValue<Map<String, String>> asMap();

    void put(Config config);

    <T> void set(ConfigValue<T> configValue);

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
