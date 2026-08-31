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
import org.elasticsearch.transport.netty4.Netty4Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@ApplicationScoped
public class EmbeddedElasticNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedElasticNode.class);

    protected final ConfigService configService;

    protected Node elasticNode;

    @Inject
    public EmbeddedElasticNode(ConfigService configService) {
        this.configService = configService;

        LOGGER.warn("EmbeddedElasticNode is added as a dependency of the Project. Replace with a standalone instance for best performance");
        if (configService.getAsBoolean("elastic.embedded.autostart").orElse(false)) {
            startNode();
        }
    }

    public void startNode() {
        LOGGER.info("Starting EmbeddedElasticNode");
        if (elasticNode != null) {
            LOGGER.warn("The EmbeddedElasticNode is already running");
            return;
        }

        String dataPath = configService.getAsString("elastic.embedded.dataPath").orElse("./data");
        LOGGER.debug("EmbeddedElasticNode DataPath [{}]", dataPath);

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

        try {
            this.elasticNode = NodeFactory.createNode(environment, Set.of(Netty4Plugin.class));
            this.elasticNode.start();
            LOGGER.info("EmbeddedElasticNode started successfully");
        } catch (Exception ex) {
            LOGGER.error("Failed to start EmbeddedElasticNode", ex);
            this.elasticNode = null;
        }
    }

    public void stopNode() {
        if (this.elasticNode == null) {
            LOGGER.warn("EmbeddedElasticNode isn't running");
            return;
        }
        try {
            this.elasticNode.close();
            this.elasticNode = null;
            LOGGER.info("EmbeddedElasticNode stopped successfully");
        } catch (Exception ex) {
            LOGGER.error("Failed to stop EmbeddedElasticNode", ex);
        }

    }

    public void onStartUp(@Observes @Priority(PriorityConstants.PLATFORM_BEFORE) ApplicationPreReadyEvent preReadyEvent) {
        LOGGER.debug("ApplicationPreReadyEvent processed");
    }
}
