package com.neo.util.framework.impl.component;

import com.neo.util.framework.api.component.ApplicationComponent;
import com.neo.util.framework.api.config.Config;
import com.neo.util.framework.api.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ApplicationComponentManager {

    public static final String CONFIG_PREFIX = "component.";
    public static final String CONFIG_ENABLED = "enabled";
    protected Set<String> componentIds;

    protected Map<String, Config> componentConfig;

    @Inject
    protected ConfigService configService;

    @Inject
    protected void init(Instance<ApplicationComponent> components) {
        this.componentIds = components.stream().map(ApplicationComponent::componentId).collect(Collectors.toSet());
    }

    @PostConstruct
    public void reload() {
        Map<String, Config> configMap = new HashMap<>();
        for (String component: componentIds) {
            configMap.put(component, configService.get(CONFIG_PREFIX + component));
        }
        componentConfig = Collections.unmodifiableMap(configMap);
    }

    public Config getComponentConfig(String componentId) {
        return componentConfig.get(componentId);
    }

    public Optional<Boolean> isComponentEnabled(String componentId) {
        return componentConfig.get(componentId).get(CONFIG_ENABLED).asBoolean().asOptional();
    }
}
