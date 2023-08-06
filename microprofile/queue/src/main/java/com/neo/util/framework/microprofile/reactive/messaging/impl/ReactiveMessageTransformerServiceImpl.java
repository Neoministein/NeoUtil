package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformer;
import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class ReactiveMessageTransformerServiceImpl implements ReactiveMessageTransformerService {

    @Inject
    protected ConfigService configService;

    protected ReactiveMessageTransformer defaultTransformer;

    protected Map<String, ReactiveMessageTransformer> transformerMap = new HashMap<>();

    @Inject
    public void init(Instance<ReactiveMessageTransformer> instance, DummyTransformer dummyTransformer) {
        Optional<String> optDefaultTransformer = configService.get("queue").get("defaultTransformer").asString().asOptional();

        for (ReactiveMessageTransformer transformer: instance) {
            transformerMap.put(transformer.getTransformerId(), transformer);
            if (optDefaultTransformer.isPresent() && transformer.getTransformerId().equals(optDefaultTransformer.get())) {
                defaultTransformer = transformer;
            }
        }

        if (defaultTransformer == null) {
            defaultTransformer = dummyTransformer;
        }
    }

    @Override
    public ReactiveMessageTransformer getTransformer(String queueName) {
        return configService.get("queue").get(queueName).asString()
                .map(s -> transformerMap.get(s))
                .orElse(defaultTransformer);
    }
}
