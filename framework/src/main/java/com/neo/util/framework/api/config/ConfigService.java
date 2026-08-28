package com.neo.util.framework.api.config;

import com.neo.util.common.impl.exception.ExceptionDetails;

import java.util.List;
import java.util.function.Function;

/**
 * This interface provides access to global config values
 * <p>
 * It represents the basic .properties structure, example: <br>
 * - security.enabled=true -> getAsBoolean("security.enabled") <br>
 *
 * List are internally represented as: - security.cors.0=... -
 * security.cors.1=...
 */
public interface ConfigService {

    ExceptionDetails EX_CONFIG_PARSING = new ExceptionDetails("config/parsing",
            "The config [{0}] with value [{1}] cannot be converted to [{2}]");

    /**
     * Persists the config.
     */
    void save(Object value, String... key);

    /**
     * Returns the {@link ConfigValue} of this config
     *
     * @param clazz the class which the {@link ConfigValue} should be
     * @param mapper the function to map the string based value to the given format
     * @param <T> the class
     * @param key of the config value
     *
     * @return the config value of the config
     */
    <T> ConfigValue<T> getAs(Class<T> clazz, Function<String, T> mapper, String... key);

    /**
     * The {@link ConfigValue} as a {@link Boolean}
     */
    default ConfigValue<Boolean> getAsBoolean(String... key) {
        return this.getAs(Boolean.class, Boolean::parseBoolean, key);
    }

    /**
     * The {@link ConfigValue} as a {@link String}
     */
    default ConfigValue<String> getAsString(String... key) {
        return this.getAs(String.class, x -> x, key);
    }

    /**
     * The {@link ConfigValue} as a {@link Integer}
     */
    default ConfigValue<Integer> getAsInt(String... key) {
        return this.getAs(Integer.class, Integer::parseInt, key);
    }

    /**
     * The {@link ConfigValue} as a {@link Long}
     */
    default ConfigValue<Long> getAsLong(String... key) {
        return this.getAs(Long.class, Long::parseLong, key);
    }

    /**
     * The {@link ConfigValue} as a {@link Double}
     */
    default ConfigValue<Double> getAsDouble(String... key) {
        return this.getAs(Double.class, Double::parseDouble, key);
    }

    /**
     * The {@link ConfigValue} as a {@link List} of the provided class
     */
    <T> ConfigValue<List<T>> getAsList(Class<T> clazz, Function<String, T> mapper, String... key);
}