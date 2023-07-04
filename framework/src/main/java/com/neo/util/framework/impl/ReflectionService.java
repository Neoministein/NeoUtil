package com.neo.util.framework.impl;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.framework.api.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

@ApplicationScoped
public class ReflectionService {

    @Inject
    protected ConfigService configService;

    @Inject
    protected JandexService jandexService;

    protected boolean useJandex;

    public ReflectionService() {
        //Required for CDI
    }

    public ReflectionService(JandexService jandexService) {
        //For testing purposes
        this.jandexService = jandexService;
        this.useJandex = jandexService != null;
    }

    @PostConstruct
    public void reload() {
        useJandex = jandexService.jandexFilesFound() &&
                configService.get("reflection.useJandex").asBoolean().orElse(true);

    }

    public Set<AnnotatedElement> getAnnotatedElement(Class<? extends Annotation> annotation) {
        if (useJandex) {
            return jandexService.getAnnotatedElement(annotation);
        } else {
            return ReflectionUtils.getAnnotatedElement(annotation);
        }
    }
}
