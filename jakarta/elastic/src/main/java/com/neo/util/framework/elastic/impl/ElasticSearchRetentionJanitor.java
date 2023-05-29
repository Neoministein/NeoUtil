package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.janitor.JanitorJob;
import com.neo.util.framework.api.persistence.search.SearchRetentionStrategy;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.elastic.api.IndexNamingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class ElasticSearchRetentionJanitor implements JanitorJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRetentionJanitor.class);

    @Inject
    protected IndexNamingService indexNamingService;

    @Inject
    protected SearchRetentionStrategy searchRetentionStrategy;

    @Inject
    protected ElasticSearchProvider elasticSearchProvider;

    public void execute(LocalDate now) {
        LOGGER.info("Starting cleanup for elastic indices...");
        for (Class<? extends Searchable> searchable: indexNamingService.getAllSearchables()) {
            LOGGER.debug("Cleaning up indices for Searchable [{}]", searchable.getSimpleName());
            SearchableIndex searchableIndex = searchable.getAnnotation(SearchableIndex.class);
            Collection<String> indices = elasticSearchProvider.getIndicesOfSearchable(searchable);

            for (String index: indices) {
                Optional<LocalDate> localDate = indexNamingService.getDateFromIndexName(searchable, index);
                if (localDate.isPresent()
                        && (searchRetentionStrategy.shouldIndexBeDeleted(now, localDate.get(), searchableIndex))) {
                        elasticSearchProvider.deleteIndex(index);
                }
            }
        }
        LOGGER.info("Finished cleanup for elastic indices");
    }
}