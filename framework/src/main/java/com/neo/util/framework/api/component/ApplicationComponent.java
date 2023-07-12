package com.neo.util.framework.api.component;

/**
 * This class defines small application components which can be reloaded and disabled
 */
public interface ApplicationComponent {

    default String componentId() {
        return this.getClass().getSimpleName();
    }
}
