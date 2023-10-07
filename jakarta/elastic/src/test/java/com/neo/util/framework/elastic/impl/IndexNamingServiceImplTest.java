package com.neo.util.framework.elastic.impl;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.reflection.IndexReflectionProvider;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.impl.ReflectionService;
import com.neo.util.framework.impl.config.BasicConfigService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

public class IndexNamingServiceImplTest {

    IndexNamingServiceImpl subject;

    BasicConfigService basicConfigService;

    @Before
    public void before() {
        basicConfigService = new BasicConfigService(new HashMap<>());

        subject = new IndexNamingServiceImpl() {
            {
                configService = basicConfigService;
                searchableIndexCache = new HashMap<>();
            }
        };
        subject.postConstruct();
        subject.initIndexProperties(new ReflectionService(new IndexReflectionProvider(ThreadUtils.classLoader())));
    }

    @Test
    public void test() {
        LocalDate creationDate = LocalDate.of(2023, 1,1);

        Optional<LocalDate> dailyIndex = subject.getDateFromIndexName(DailySearchable.class, DailySearchable.INDEX_NAME + "-" + IndexNamingServiceImpl.INDEX_DATE_FORMAT_DAY.format(creationDate) +"-v1");
        Assert.assertEquals(creationDate, dailyIndex.orElseThrow());

        Optional<LocalDate> weeklyIndex = subject.getDateFromIndexName(WeeklySearchable.class, WeeklySearchable.INDEX_NAME + "-" + IndexNamingServiceImpl.INDEX_DATE_FORMAT_WEEK.format(creationDate) +"-v1");
        Assert.assertEquals(creationDate, weeklyIndex.orElseThrow());

        Optional<LocalDate> monthlyIndex = subject.getDateFromIndexName(MonthlySearchable.class, MonthlySearchable.INDEX_NAME + "-"+ IndexNamingServiceImpl.INDEX_DATE_FORMAT_MONTH.format(creationDate) +"-v1");
        Assert.assertEquals(creationDate, monthlyIndex.orElseThrow());

        Optional<LocalDate> yearlyIndex = subject.getDateFromIndexName(YearlySearchable.class, YearlySearchable.INDEX_NAME + "-"+ IndexNamingServiceImpl.INDEX_DATE_FORMAT_YEAR.format(creationDate) +"-v1");
        Assert.assertEquals(creationDate, yearlyIndex.orElseThrow());
    }

    @SearchableIndex(indexName = DailySearchable.INDEX_NAME, indexPeriod = IndexPeriod.DAILY)
    public static class DailySearchable extends AbstractSearchable {
        public static final String INDEX_NAME = "daily-index-name";
    }

    @SearchableIndex(indexName = WeeklySearchable.INDEX_NAME, indexPeriod = IndexPeriod.WEEKLY)
    public static class WeeklySearchable extends AbstractSearchable {
        public static final String INDEX_NAME = "weekly-index-name";
    }

    @SearchableIndex(indexName = MonthlySearchable.INDEX_NAME, indexPeriod = IndexPeriod.MONTHLY)
    public static class MonthlySearchable extends AbstractSearchable {
        public static final String INDEX_NAME = "monthly-index-name";
    }

    @SearchableIndex(indexName = YearlySearchable.INDEX_NAME, indexPeriod = IndexPeriod.YEARLY)
    public static class YearlySearchable extends AbstractSearchable {
        public static final String INDEX_NAME = "yearly-index-name";
    }

    public static abstract class AbstractSearchable implements Searchable {
        @Override
        public String getBusinessId() {
            return null;
        }

        @Override
        public Instant getCreationDate() {
            return null;
        }

        @Override
        public Long getVersion() {
            return null;
        }
    }
}