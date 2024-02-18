package com.neo.util.framework.elastic.impl;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;
import com.neo.util.framework.elastic.api.IndexNamingService;
import com.neo.util.framework.impl.ReflectionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class IndexNamingServiceImpl implements IndexNamingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexNamingServiceImpl.class);

    protected static final String INDEX_SEPARATOR = "-";
    protected static final String SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX = "no-date";

    public static final String CONFIG_PREFIX = ElasticSearchProvider.CONFIG_PREFIX;

    protected static final String PROJECT_PREFIX_CONFIG = CONFIG_PREFIX + ".prefix";
    protected static final String PROJECT_POSTFIX_CONFIG = CONFIG_PREFIX + ".postfix";
    protected static final String MAPPING_VERSION_CONFIG = CONFIG_PREFIX + ".mappingVersion";

    protected static final String DEFAULT_MAPPING_VERSION = "v1";

    protected static final DateTimeFormatter INDEX_DATE_FORMAT_DAY = DateTimeFormatter.ofPattern("yyyy.MM.ww.DDD")
            .withZone(ZoneId.systemDefault());
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_WEEK = DateTimeFormatter.ofPattern("yyyy.MM.ww")
            .withZone(ZoneId.systemDefault());
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_MONTH = DateTimeFormatter.ofPattern("yyyy.MM")
            .withZone(ZoneId.systemDefault());
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_YEAR = DateTimeFormatter.ofPattern("yyyy")
            .withZone(ZoneId.systemDefault());

    protected final String mappingVersion;
    protected final String indexPrefix;
    protected final String indexPostFix;

    /** Cache that saves the annotation information of each searchable */
    protected final Map<Class<? extends Searchable>, SearchableIndex> searchableIndexCache = new HashMap<>();

    @Inject
    public IndexNamingServiceImpl(ConfigService configService, ReflectionService reflectionService) {
        for (AnnotatedElement annotatedElement: reflectionService.getAnnotatedElement(SearchableIndex.class)) {
            Class<? extends Searchable> searchableClass = (Class<? extends Searchable>) annotatedElement;
            searchableIndexCache.put(searchableClass, searchableClass.getAnnotation(SearchableIndex.class));
        }

        mappingVersion = configService.get(MAPPING_VERSION_CONFIG).asString().orElse(DEFAULT_MAPPING_VERSION);

        String prefix = configService.get(PROJECT_PREFIX_CONFIG).asString().orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(prefix)) {
            prefix = prefix.toLowerCase() + INDEX_SEPARATOR;
        }
        this.indexPrefix = prefix;
        String postfix = configService.get(PROJECT_POSTFIX_CONFIG).asString().orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(postfix)) {
            postfix = INDEX_SEPARATOR + postfix.toLowerCase();
        }
        this.indexPostFix = postfix;
    }

    public String getIndexName(Searchable searchable) {

        StringBuilder sb = new StringBuilder();

        sb.append(getIndexNamePrefixFromClass(searchable.getClass(), true));
        IndexPeriod indexPeriod = searchableIndexCache.get(searchable.getClass()).indexPeriod();

        if (!IndexPeriod.EXTERNAL.equals(indexPeriod)) {

            String postfix = getIndexNamePostfix(indexPeriod, searchable);
            if (!StringUtils.isEmpty(postfix)) {
                sb.append(INDEX_SEPARATOR).append(postfix);
            }
            sb.append(INDEX_SEPARATOR).append(mappingVersion.toLowerCase());
        }
        return sb.toString();
    }

    public String getIndexNamePrefixFromClass(Class<? extends Searchable> searchableClazz, boolean appendInfix) {
        return (appendInfix  ? getIndexPrefix() : StringUtils.EMPTY) + searchableIndexCache.get(searchableClazz).indexName() + (appendInfix  ? getIndexPostfix() : StringUtils.EMPTY);
    }


    @Override
    public String getIndexPostfix() {
        return indexPostFix;
    }

    @Override
    public Set<Class<? extends Searchable>> getAllSearchables() {
        return searchableIndexCache.keySet();
    }

    @Override
    public Optional<LocalDate> getDateFromIndexName(Class<? extends Searchable> searchableClass, String indexName) {
        IndexPeriod indexPeriod = searchableIndexCache.get(searchableClass).indexPeriod();

        DateTimeFormatter formatter = getDateFormatter(indexPeriod);
        if (formatter == null) {
            LOGGER.debug("There is no date stored in the index name");
            return Optional.empty();
        }

        String date;
        try {
            date = indexName.substring(getIndexNamePrefixFromClass(searchableClass, true).length() + 1, indexName.lastIndexOf('-'));
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.warn("Unable to extract the date from index name [{}]", indexName);
            return Optional.empty();
        }

        try {
            return switch (indexPeriod) {
                case DAILY -> Optional.of(LocalDate.parse(date, formatter));
                case WEEKLY, MONTHLY -> {
                    TemporalAccessor temporalAccessor = formatter.parse(date);
                    yield Optional.of(LocalDate.of(temporalAccessor.get(ChronoField.YEAR), temporalAccessor.get(ChronoField.MONTH_OF_YEAR), 1));
                }
                case YEARLY -> {
                    TemporalAccessor temporalAccessor = formatter.parse(date);
                    yield Optional.of(LocalDate.of(temporalAccessor.get(ChronoField.YEAR), 1, 1));
                }
                default -> Optional.empty();
            };
        } catch (DateTimeParseException ex) {
            LOGGER.warn("Unable to parse the date from the index name [{}]", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public String getIndexPrefix() {
        return indexPrefix;
    }

    /**
     * Returns the index postfix. It is controlled by the index period
     */
    protected String getIndexNamePostfix(IndexPeriod indexPeriod, Searchable searchable) {
        if (IndexPeriod.EXTERNAL.equals(indexPeriod)) {
            return StringUtils.EMPTY;
        }
        return getDateFormatString(searchable.getCreationDate(), getDateFormatter(indexPeriod));
    }

    /**
     * Returns the appropriate date formatter for the given index period.
     */
    protected DateTimeFormatter getDateFormatter(IndexPeriod indexPeriod) {
        return switch (indexPeriod) {
            case DAILY -> INDEX_DATE_FORMAT_DAY;
            case WEEKLY -> INDEX_DATE_FORMAT_WEEK;
            case MONTHLY -> INDEX_DATE_FORMAT_MONTH;
            case YEARLY -> INDEX_DATE_FORMAT_YEAR;
            case ALL, EXTERNAL -> null;
        };
    }

    /**
     * Formats the given {@link Instant} with the given formatter. When the formatter is null the default value will be
     * returned.
     */
    protected String getDateFormatString(Instant dateTime, DateTimeFormatter formatter) {
        if (formatter != null) {
            return formatter.format(dateTime);
        } else {
            return SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX;
        }
    }
}
