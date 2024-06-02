package com.neo.util.elastic;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.event.ApplicationPreReadyEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.elasticsearch.common.logging.LogConfigurator;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeFactory;
import org.elasticsearch.plugins.EmbeddedPluginsService;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.transport.netty4.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Function;

@ApplicationScoped
public class ElasticEmbedded {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticEmbedded.class);

    protected final ConfigService configService;

    protected Node elasticNode;

    @Inject
    public ElasticEmbedded(ConfigService configService) {
        this.configService = configService;

        if (configService.get("elastic.embedded.autostart").asBoolean().orElse(false)) {
            startNode();
        }
    }

    public void startNode() {
        if (elasticNode != null) {
            LOGGER.warn("");
            return;
        }

        String dataPath = configService.get("elastic.embedded.dataPath").asString().orElse("./data");

        LogConfigurator.registerErrorListener();
        LogConfigurator.configureESLogging();
        // Create settings for the embedded node
        Settings settings = Settings.builder()
                .put("path.home", dataPath)
                .put("node.name", "embedded")
                .put(NetworkModule.TRANSPORT_TYPE_KEY, Netty4Plugin.NETTY_TRANSPORT_NAME)
                .put(NetworkModule.HTTP_TYPE_KEY, Netty4Plugin.NETTY_HTTP_TRANSPORT_NAME)
                .build();

        // Create environment
        Path pathHome = Paths.get(dataPath);
        Environment environment = new Environment(settings, pathHome);

        // Create the embedded node
        Function<Settings, PluginsService> pluginServiceCtor = settings1 -> new EmbeddedPluginsService(settings1, environment, Set.of(Netty4Plugin.class));
        try {
            this.elasticNode = NodeFactory.createNode(environment, pluginServiceCtor, true);
            this.elasticNode.start();
        } catch (Exception ex) {
            this.elasticNode = null;
        }

    }

    public void stopNode() {
        if (this.elasticNode == null) {
            LOGGER.warn("");
            return;
        }
        try {
            this.elasticNode.close();
            this.elasticNode = null;
        } catch (IOException ex) {
            LOGGER.error("");
        }

    }

    public void onStartUp(@Observes @Priority(PriorityConstants.PLATFORM_BEFORE) ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
    }
}
