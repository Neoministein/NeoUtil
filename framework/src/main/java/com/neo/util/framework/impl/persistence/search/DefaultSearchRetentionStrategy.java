package com.neo.util.framework.impl.persistence.search;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.RetentionPeriod;
import com.neo.util.framework.api.persistence.search.SearchRetentionStrategy;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@ApplicationScoped
public class DefaultSearchRetentionStrategy implements SearchRetentionStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSearchRetentionStrategy.class);

    public static final String CONFIG_PREFIX = "search.retention";
    public static final String DAILY_CONFIG = CONFIG_PREFIX + ".daily";
    public static final String WEEKLY_CONFIG = CONFIG_PREFIX + ".weekly";
    public static final String MONTHLY_CONFIG = CONFIG_PREFIX + ".monthly";
    public static final String YEARLY_CONFIG = CONFIG_PREFIX + ".yearly";

    public static final String CUSTOM_RETENTION_CONFIG = CONFIG_PREFIX + ".custom.";

    protected final ConfigService configService;

    @Inject
    public DefaultSearchRetentionStrategy(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean shouldIndexBeDeleted(LocalDate now, LocalDate creationDate, SearchableIndex searchableIndex) {
        LOGGER.info("Checking if index [{}] should be deleted", searchableIndex.indexName());

        LOGGER.debug("The retention period of index [{}] is marked as [{}]", searchableIndex.indexName(), searchableIndex.retentionPeriod());
        if (RetentionPeriod.INDEX_BASED != searchableIndex.retentionPeriod()) {
            return false;
        }
        LOGGER.debug("The index period of index [{}] is marked as [{}]", searchableIndex.indexName(), searchableIndex.indexPeriod());
        Optional<Period> retention = getRetention(searchableIndex);
        if (retention.isEmpty()) {
            return false;
        }

        if (retention.get().getDays() < 0 ) {
            LOGGER.debug("The retention period for index [{}] is negative, therefore index won't be deleted.", searchableIndex.indexName());
            return false;
        }

        LocalDate nextRollOverDate = IndexPeriod.getNextRollOverDate(searchableIndex.indexPeriod(), creationDate).orElseThrow();

        LocalDate lastRetentionDate = nextRollOverDate.plus(retention.orElseThrow());

        if (now.isBefore(lastRetentionDate)) {
            LOGGER.debug("Index [{}] won't be deleted there are [{}] days left until deletion", searchableIndex.indexName(),
                    ChronoUnit.DAYS.between(LocalDate.now(), lastRetentionDate));
            return false;
        }

        LOGGER.info("Index [{}] can be deleted", searchableIndex.indexName());
        return true;
    }

    protected Optional<Period> getRetention(SearchableIndex searchableIndex) {
        Optional<Period> customRetention = configService.get(CUSTOM_RETENTION_CONFIG + searchableIndex.indexName()).asInt().map(Period::ofDays);
        if (customRetention.isPresent()) {
            return customRetention;
        }
        return getRetentionFromConfig(searchableIndex.indexPeriod());
    }

    protected Optional<Period> getRetentionFromConfig(IndexPeriod indexPeriod) {
        Period retention = switch (indexPeriod) {
            case DAILY -> configService.get(DAILY_CONFIG).asInt().map(Period::ofDays).orElse(Period.ofDays(7));
            case WEEKLY ->  configService.get(WEEKLY_CONFIG).asInt().map(Period::ofDays).orElse(Period.ofMonths(2));
            case MONTHLY ->  configService.get(MONTHLY_CONFIG).asInt().map(Period::ofDays).orElse(Period.ofYears(1));
            case YEARLY ->  configService.get(YEARLY_CONFIG).asInt().map(Period::ofDays).orElse(Period.ofYears(10));
            case EXTERNAL, ALL ->  null;
        };
        LOGGER.trace("The IndexPeriod [{}] retention is configured as [{}]", indexPeriod, retention);
        return Optional.ofNullable(retention);
    }
}