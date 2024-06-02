package org.elasticsearch.node;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.PluginsService;

import java.util.function.Function;

/**
 * This Class exists since the constructor is package private
 */
public class NodeFactory {

    private NodeFactory() {}

    public static Node createNode(Environment initialEnvironment,
                           Function<Settings, PluginsService> pluginServiceCtor,
                           boolean forbidPrivateIndexSettings) {
        return new Node(initialEnvironment, pluginServiceCtor, forbidPrivateIndexSettings);
    }
}
