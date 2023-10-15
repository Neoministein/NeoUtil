package com.neo.util.framework.microprofile.reactive.messaging.build.config;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.queue.IncomingQueue;
import com.neo.util.framework.api.queue.OutgoingQueue;
import com.neo.util.framework.api.queue.QueueType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

        for (OutgoingQueue annotation: context.fullReflection().getAnnotationInstance(OutgoingQueue.class)) {
            queueTypeMap.put(annotation.value(), annotation.type());

            sb.append(createOutgoing(annotation.value(), annotation.type()));
        }

        sb.append("    incoming:\n");

        for (IncomingQueue annotation: context.fullReflection().getAnnotationInstance(IncomingQueue.class)) {

            sb.append(createIncoming(annotation.value(), Optional.ofNullable(queueTypeMap.get(annotation.value())).orElse(QueueType.UNKNOWN)));
        }

        try {
            Files.writeString(Paths.get(context.targetDirectory() + "/queue.application.yml"), sb.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String createIncoming(String queueName, QueueType type) {
        return STR.
                """
                      from-\{queueName}:
                        connector: helidon-jms
                        destination: \{queueName}
                        type: \{type.name().toLowerCase()}
                """;
    }

    protected String createOutgoing(String queueName, QueueType type) {
        return STR.
               """
                      to-\{queueName}:
                        connector: helidon-jms
                        destination: \{queueName}
                        type: \{type.name().toLowerCase()}
                """;
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}