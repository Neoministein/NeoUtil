package com.neo.util.framework.microprofile.reactive.messaging.build.config;

import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.queue.OutgoingQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ActiveMQConfigBuildStep implements BuildStep {

    @Override
    public void execute(BuildContext context) {
        StringBuilder sb = new StringBuilder("<address-settings>\n");

        for (OutgoingQueue annotation: context.fullReflection().getAnnotationInstance(OutgoingQueue.class)) {
            sb.append(createAddressSettings(annotation.value(), annotation.retry(), annotation.delay(), annotation.timeUnit()));
        }
        sb.append("</address-settings>");
        try {
            Files.writeString(Paths.get(context.targetDirectory() + "/activemq.broker.xml"), sb.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected String createAddressSettings(String queueName, int retry, int delay, TimeUnit timeUnit) {
        return STR.
                """
                    <address-setting match="\{queueName}">
                       <max-delivery-attempts>\{retry}</max-delivery-attempts>
                       <redelivery-delay>\{timeUnit.toMillis(delay)}</redelivery-delay>
                    </address-setting>
                """;
    }

        @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
