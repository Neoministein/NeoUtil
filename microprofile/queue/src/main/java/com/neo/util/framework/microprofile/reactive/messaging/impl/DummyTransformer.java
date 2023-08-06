package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformer;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Function;

@ApplicationScoped
public class DummyTransformer implements ReactiveMessageTransformer {

    @Override
    public Function<String, ?> getMessageTransformer(String queueName) {
        return s -> s;
    }
}
