package com.neo.util.microprofile.reactive.messaging.transformer;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Function;

@ApplicationScoped
public class DummyTransformer implements ReactiveMessageTransformer {

    @Override
    public Function<String, ?> getMessageTransformer(String queueName) {
        return s -> s;
    }
}
