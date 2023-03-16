package com.neo.util.framework.rest.cache.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.config.ConfigValue;
import io.helidon.microprofile.tests.junit5.AddBean;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractIntegrationTest {

    protected JsonNode validateResponse(Response response, int code) {
        Assertions.assertEquals(code, response.getStatus());
        return JsonUtil.fromJson(response.readEntity(String.class));
    }

    public static class TestConfigService implements ConfigService {

        @Override
        public Config get(String key) {
            return new TestConfig();
        }

        @Override
        public void save(ConfigValue<?> configValue) {

        }
    }

    public static class TestConfig implements Config {

        @Override public String key() {
            return null;
        }

        @Override public Config get(String key) {
            return null;
        }

        @Override public Type type() {
            return null;
        }

        @Override public boolean hasValue() {
            return false;
        }

        @Override public <T> ConfigValue<T> as(Class<T> clazz) {
            return null;
        }

        @Override public <T> ConfigValue<T> as(Function<Config, T> mapper) {
            return null;
        }

        @Override public <T> ConfigValue<List<T>> asList(Class<T> clazz) {
            return null;
        }

        @Override public ConfigValue<Map<String, String>> asMap() {
            return null;
        }
    }

    public static class TestConfigValue<T> implements ConfigValue<T> {

        @Override public String key() {
            return null;
        }

        @Override public Optional<T> asOptional() {
            return Optional.empty();
        }

        @Override public T get() throws ConfigurationException {
            return null;
        }

        @Override public void set(T value) {

        }
    }
}
