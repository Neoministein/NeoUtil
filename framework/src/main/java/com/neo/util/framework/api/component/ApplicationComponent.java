package com.neo.util.framework.api.component;

/**
 * This class defines small application components which can be reloaded and disabled
 */
public interface ApplicationComponent {

    /**
     * Checks if the component is enabled
     */
    boolean enabled();

    /**
     * Reloads the components config
     */
    void reload();

    default String componentId() {
        return this.getClass().getSimpleName();
    }
}
