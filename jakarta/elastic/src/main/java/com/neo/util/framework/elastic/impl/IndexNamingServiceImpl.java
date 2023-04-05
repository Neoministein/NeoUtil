package com.neo.util.framework.elastic.impl;

import com.neo.util.common.impl.StringUtils;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.elastic.api.IndexNamingService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class IndexNamingServiceImpl implements IndexNamingService {

    protected static final String INDEX_SEPARATOR = "-";
    protected static final String SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX = "no-date";

    public static final String CONFIG_PREFIX = ElasticSearchProvider.CONFIG_PREFIX;

    protected static final String PROJECT_PREFIX_CONFIG = CONFIG_PREFIX + ".prefix";
    protected static final String PROJECT_POSTFIX_CONFIG = CONFIG_PREFIX + ".postfix";
    protected static final String MAPPING_VERSION_CONFIG = CONFIG_PREFIX + ".mappingVersion";

    protected static final String DEFAULT_MAPPING_VERSION = "v1";

    //TODO REDO WITH THREAD SAFE LOGIC
    protected static final SimpleDateFormat INDEX_DATE_FORMAT_DAY = new SimpleDateFormat("yyyy.MM.ww.DDD");
    protected static final SimpleDateFormat INDEX_DATE_FORMAT_WEEK = new SimpleDateFormat("yyyy.MM.ww");
    protected static final SimpleDateFormat INDEX_DATE_FORMAT_MONTH = new SimpleDateFormat("yyyy.MM");
    protected static final SimpleDateFormat INDEX_DATE_FORMAT_YEAR = new SimpleDateFormat("yyyy");

    protected String mappingVersion;
    protected String indexPrefix;
    protected String indexPostFix;

    @Inject
    protected ConfigService configService;

    /** Cache that saves the annotation information of each searchable in regards to its index prefix */
    protected Map<Class<?>, String> indexNamePrefixes;

    /** Cache that saves the annotation information of each searchable in regards to its index period */
    protected Map<Class<?>, IndexPeriod> indexPeriods;

    /**
     * Create a new IndexNameServiceImpl.
     */
    public IndexNamingServiceImpl() {
        super();
    }

    /**
     * postConstruct logic.
     */
    @PostConstruct
    public void postConstruct() {

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

    /**
     * Initializes the index names for each searchable. This is done only once at startup as it is defined by the
     * annotations on the searchable and cannot be changed at runtime.
     */
    @Inject //TODO USE JANDEX INSTEAD OF CDI FOR SEARCHABLE LOOKUP
    public void initIndexProperties(@Any Instance<Searchable> searchables) {
        indexNamePrefixes = new HashMap<>();
        indexPeriods = new HashMap<>();

        for (Searchable searchable : searchables) {
            indexNamePrefixes.put(searchable.getClass(), getIndexNamePrefix(searchable, false));
            indexPeriods.put(searchable.getClass(), searchable.getIndexPeriod());
        }
    }

    public String getIndexName(Searchable searchable) {
        StringBuilder sb = new StringBuilder();

        sb.append(getIndexNamePrefix(searchable, true));

        if (!IndexPeriod.EXTERNAL.equals(searchable.getIndexPeriod())) {

            String postfix = getIndexNamePostfix(searchable.getIndexPeriod(), searchable);
            if (!StringUtils.isEmpty(postfix)) {
                sb.append(INDEX_SEPARATOR).append(postfix);
            }
            sb.append(INDEX_SEPARATOR).append(mappingVersion.toLowerCase());
        }
        return sb.toString();
    }

    protected String getIndexNamePrefix(Searchable searchable, boolean appendInfix) {
        return (appendInfix  ? getIndexPrefix() : StringUtils.EMPTY) + searchable.getIndexName() + (appendInfix  ? getIndexPostfix() : StringUtils.EMPTY);
    }

    public String getIndexNamePrefixFromClass(Class<? extends Searchable> searchableClazz, boolean appendInfix) {
        return (appendInfix  ? getIndexPrefix() : StringUtils.EMPTY) + indexNamePrefixes.get(searchableClazz) + (appendInfix  ? getIndexPostfix() : StringUtils.EMPTY);
    }


    @Override
    public String getIndexPostfix() {
        return indexPostFix;
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
    protected SimpleDateFormat getDateFormatter(IndexPeriod indexPeriod) {
        return switch (indexPeriod) {
            case DAILY -> INDEX_DATE_FORMAT_DAY;
            case WEEKLY -> INDEX_DATE_FORMAT_WEEK;
            case MONTHLY -> INDEX_DATE_FORMAT_MONTH;
            case YEARLY -> INDEX_DATE_FORMAT_YEAR;
            case ALL, EXTERNAL -> null;
            case DEFAULT -> getDateFormatter(IndexPeriod.getDefault());
            default -> getDateFormatter(IndexPeriod.getDefault());
        };
    }

    /**
     * Formats the given {@link TemporalAccessor} with the given formatter. When the formatter is null the default value will be
     * returned.
     */
    protected String getDateFormatString(Date dateTime, SimpleDateFormat formatter) {
        if (formatter != null) {
            return formatter.format(dateTime);
        } else {
            return SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX;
        }
    }

}
