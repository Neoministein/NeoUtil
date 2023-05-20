package com.neo.util.framework.api.persistence.search;

import java.time.LocalDate;

/**
 * Implementations are used to deter the strategy if a index should be deleted.
 */
public interface SearchRetentionStrategy {

    /**
     * Checks if the index can be deleted
     *
     * @param now the current date used for retention calculation
     * @param creationDate the date the index was created
     * @param searchableIndex the information about the index
     *
     * @return true of the index can be deleted
     */
    boolean shouldIndexBeDeleted(LocalDate now, LocalDate creationDate, SearchableIndex searchableIndex);
}