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

    protected final ConfigService configService;
    protected final ReactiveMessageTransformer defaultTransformer;
    protected final Map<String, ReactiveMessageTransformer> transformerMap = new HashMap<>();

    @Inject
    public ReactiveMessageTransformerServiceImpl(ConfigService configService, Instance<ReactiveMessageTransformer> instance, DummyTransformer dummyTransformer) {
        Optional<String> optDefaultTransformer = configService.getAsString("queue.defaultTransformer").asOptional();

        ReactiveMessageTransformer defaultTransformer = null;

        for (ReactiveMessageTransformer transformer: instance) {
            transformerMap.put(transformer.getTransformerId(), transformer);
            if (optDefaultTransformer.isPresent() && transformer.getTransformerId().equals(optDefaultTransformer.get())) {
                defaultTransformer = transformer;
            }
        }

        if (defaultTransformer == null) {
            this.defaultTransformer = dummyTransformer;
        } else {
            this.defaultTransformer = defaultTransformer;
        }
        this.configService = configService;
    }

    @Override
    public ReactiveMessageTransformer getTransformer(String queueName) {
        return configService.getAsString("queue", queueName)
                .map(transformerMap::get)
                .orElse(defaultTransformer);
    }
}
