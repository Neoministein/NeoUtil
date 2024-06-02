package com.neo.util.helidon.rest;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

@HelidonTest
class ConfigTest extends AbstractIntegrationTest {

    @Inject
    protected Config rootConfig;

    @Test
    void test() {
        handleConfig("root", rootConfig);
    }

    void handleConfig(String path, Config config) {
        String newPath = path + config.key().name();
        ConfigValue<List<Config>> nodes = config.asNodeList();
        if (nodes.isEmpty()) {
            if (config.exists()) {
                System.out.println(newPath + ": " + config.asString().get());
            }
            return;
        }
        for (Config node: nodes.get()) {
            if (node.isLeaf()) {

                System.out.println(newPath + "." + node.key().name() + ": " + node.asString().get());
            } else {
                handleConfig(newPath + "." + node.key().name() + "." ,node);
            }
        }
    }
}
