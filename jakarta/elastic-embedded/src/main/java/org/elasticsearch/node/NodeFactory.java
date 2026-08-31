package org.elasticsearch.node;

import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.EmbeddedPluginsService;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginsLoader;
import org.elasticsearch.plugins.PluginsService;

import java.util.Collection;

/**
 * This Class exists since the constructor is package private
 */
public class NodeFactory {

    private NodeFactory() {}

    public static Node createNode(Environment environment,
                                  Collection<Class<? extends Plugin>> classpathPlugin) {
        return new Node(NodeConstruction.prepareConstruction(environment, null, new NodeServiceProvider(){
            @Override
            PluginsService newPluginService(Environment initialEnvironment, PluginsLoader pluginsLoader) {
                // this creates a PluginsService with an empty list of classpath plugins
                return new EmbeddedPluginsService(initialEnvironment.settings(),initialEnvironment, classpathPlugin);
            }
        }, true));
    }
}
