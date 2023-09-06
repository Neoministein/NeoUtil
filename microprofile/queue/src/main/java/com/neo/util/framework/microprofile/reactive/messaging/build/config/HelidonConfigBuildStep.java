package com.neo.util.framework.microprofile.reactive.messaging.build.config;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HelidonConfigBuildStep implements BuildStep {

    @Override
    public void execute(BuildContext context) {
        StringBuilder sb = new StringBuilder("""
                mp:
                  messaging:
                    connector.helidon-jms:
                      jndi:
                        jms-factory: ConnectionFactory
                        env-properties:
                          java.naming:
                            factory.initial: class.to.factory
                            provider.url: ${org.apache.activemq.url}
                """);

        Map<String, QueueType> queueTypeMap = new HashMap<>();

        sb.append("    outgoing:\n");

        for (OutgoingQueue annotation: ReflectionUtils.getAnnotationInstance(OutgoingQueue.class, context.fullLoader())) {
            queueTypeMap.put(annotation.value(), annotation.type());

            sb.append(createOutgoing(annotation.value(), annotation.type()));
        }

        sb.append("    incoming:\n");

        for (IncomingQueue annotation: ReflectionUtils.getAnnotationInstance(IncomingQueue.class, context.fullLoader())) {

            sb.append(createIncoming(annotation.value(), Optional.ofNullable(queueTypeMap.get(annotation.value())).orElse(QueueType.UNKNOWN)));
        }

        try {
            Files.writeString(Paths.get(context.targetDirectory() + "/queue.application.yml"), sb.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String createIncoming(String queueName, QueueType type) {
        String format = """
                      from-{0}:
                        connector: helidon-jms
                        destination: {0}
                        type: {1}
                """;
        return MessageFormat.format(format, queueName, type.name().toLowerCase());
    }

    protected String createOutgoing(String queueName, QueueType type) {
        String format = """
                      to-{0}:
                        connector: helidon-jms
                        destination: {0}
                        type: {1}
                """;
        return MessageFormat.format(format, queueName, type.name().toLowerCase());
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}