package com.neo.util.framework.elastic.impl;

import com.neo.util.api.config.ConfigService;
import com.neo.util.api.config.DefaultConfigService;
import com.neo.util.api.persistence.search.IndexPeriod;
import com.neo.util.api.persistence.search.RetentionPeriod;
import com.neo.util.api.persistence.search.SearchableIndex;
import com.neo.util.jakarta.config.InMemoryConfigStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.List;

public class DefaultSearchRetentionStrategyTest {

    private static final String INDEX_NAME = "indexName";


    DefaultSearchRetentionStrategy subject;

    ConfigService basicConfigService;

    @Before
    public void before() {
        basicConfigService = new DefaultConfigService(List.of(new InMemoryConfigStore()));

        subject = new DefaultSearchRetentionStrategy(basicConfigService);
    }

    @Test
    public void basicNotDeletedTest() {
        boolean retentionExternal = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.DAILY, RetentionPeriod.EXTERNAL));
        boolean indexPeriodAll = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.ALL, RetentionPeriod.INDEX_BASED));
        boolean indexPeriodExternal = subject.shouldIndexBeDeleted(null, null, getSearchableIndex(IndexPeriod.EXTERNAL, RetentionPeriod.INDEX_BASED));


        Assert.assertFalse(retentionExternal);
        Assert.assertFalse(indexPeriodAll);
        Assert.assertFalse(indexPeriodExternal);
    }

    @Test
    public void dailyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 8), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 9), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void weeklyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.WEEKLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 3, 1), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 3, 2), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void monthlyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.MONTHLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2024, 1, 31), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2024, 2, 1), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void yearlyIndexTest() {
        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.YEARLY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2033, 1, 1), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2034, 1, 1), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void dailyConfigIndexTest() {
        basicConfigService.save(10, DefaultSearchRetentionStrategy.DAILY_CONFIG);

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 11), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 12), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void dailyIndexConfigTest() {
        basicConfigService.save( 15, DefaultSearchRetentionStrategy.CUSTOM_RETENTION_CONFIG + INDEX_NAME);

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 16), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);

        result = subject.shouldIndexBeDeleted(LocalDate.of(2023, 1, 17), indexCreateDate, searchableIndex);
        Assert.assertTrue(result);
    }

    @Test
    public void negativeRetentionConfigTest() {
        basicConfigService.save(-1, DefaultSearchRetentionStrategy.CUSTOM_RETENTION_CONFIG + INDEX_NAME);

        SearchableIndex searchableIndex = getSearchableIndex(IndexPeriod.DAILY);
        LocalDate indexCreateDate = LocalDate.of(2023, 1, 1);

        boolean result;

        result = subject.shouldIndexBeDeleted(LocalDate.of(9999, 1, 1), indexCreateDate, searchableIndex);
        Assert.assertFalse(result);
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
