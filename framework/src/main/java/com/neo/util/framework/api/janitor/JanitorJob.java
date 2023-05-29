package com.neo.util.framework.api.janitor;

import java.time.LocalDate;

/**
 * This is an interface for a single cleanup job
 */
public interface JanitorJob {

    /**
     * Executes the janitor the provided current date
     *
     * @param now current date
     */
    void execute(LocalDate now);

    /**
     * A unique Janitor id
     *
     * @return a unique Janitor id
     */
    default String getJanitorId() {
        return this.getClass().getSimpleName();
    }
}
