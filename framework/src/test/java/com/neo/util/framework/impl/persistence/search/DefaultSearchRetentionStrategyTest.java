package com.neo.util.framework.impl.persistence.search;

import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.RetentionPeriod;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.impl.config.BasicConfigService;
import com.neo.util.framework.impl.config.BasicConfigValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.HashMap;

class DefaultSearchRetentionStrategyTest {

    private static final String INDEX_NAME = "indexName";


    DefaultSearchRetentionStrategy subject;

    BasicConfigService basicConfigService;

    @BeforeEach
    void before() {
        subject = new DefaultSearchRetentionStrategy();

        basicConfigService = new BasicConfigService(new HashMap<>());
        subject.configService = basicConfigService;
    }

    @Test
    void basicNotDeletedTest() {
        boolean retentionExternal = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.DAILY, RetentionPeriod.EXTERNAL));
        boolean indexPeriodAll = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.ALL, RetentionPeriod.INDEX_BASED));
        boolean indexPeriodExternal = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.EXTERNAL, RetentionPeriod.INDEX_BASED));


        Assertions.assertFalse(retentionExternal);
        Assertions.assertFalse(indexPeriodAll);
        Assertions.assertFalse(indexPeriodExternal);
    }

    @Test
    void dailyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 8), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 9), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void weeklyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.WEEKLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 3, 1), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 3, 2), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void monthlyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.MONTHLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2024, 1, 31), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2024, 2, 1), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void yearlyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.YEARLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2033, 1, 1), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2034, 1, 1), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void dailyConfigIndexTest() {
        basicConfigService.save(new BasicConfigValue<>(DefaultSearchRetentionStrategy.DAILY_CONFIG, 10));

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 11), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 12), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void dailyIndexConfigTest() {
        basicConfigService.save(new BasicConfigValue<>(DefaultSearchRetentionStrategy.CUSTOM_RETENTION_CONFIG + INDEX_NAME, 15));

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 16), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 17), indexCreateDate, searchableIndex);
        Assertions.assertTrue(result);
    }

    @Test
    void negativeRetentionConfigTest() {
        basicConfigService.save(new BasicConfigValue<>(DefaultSearchRetentionStrategy.CUSTOM_RETENTION_CONFIG + INDEX_NAME, -1));

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(9999, 1, 1), indexCreateDate, searchableIndex);
        Assertions.assertFalse(result);
    }

    protected SearchableIndex getSearchableIndex(IndexPeriod indexPeriod) {
        return getSearchableIndex(indexPeriod, RetentionPeriod.INDEX_BASED);
    }

    protected SearchableIndex getSearchableIndex(IndexPeriod indexPeriod, RetentionPeriod retentionPeriod) {
        return new SearchableIndex()
        {
            @Override
            public String indexName() {
                return INDEX_NAME;
            }

            @Override
            public IndexPeriod indexPeriod() {
                return indexPeriod;
            }

            @Override
            public RetentionPeriod retentionPeriod() {
                return retentionPeriod;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return SearchableIndex.class;
            }
        };
    }
}
