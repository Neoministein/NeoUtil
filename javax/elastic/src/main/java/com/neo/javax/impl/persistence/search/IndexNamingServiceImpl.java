package com.neo.javax.impl.persistence.search;

import com.neo.common.impl.StringUtils;
import com.neo.javax.api.config.Config;
import com.neo.javax.api.config.ConfigService;
import com.neo.javax.api.persistence.entity.IndexNamingService;
import com.neo.javax.api.persitence.search.IndexPeriod;
import com.neo.javax.api.persitence.search.Searchable;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class IndexNamingServiceImpl implements IndexNamingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexNamingServiceImpl.class);

    protected static final String INDEX_SEPARATOR = "-";
    protected static final String SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX = "no-date";

    protected static final String PROJECT_PREFIX_CONFIG = "prefix";
    protected static final String PROJECT_POSTFIX_CONFIG = "postfix";
    protected static final String MAPPING_VERSION_CONFIG = "mappingVersion";

    protected static final String DEFAULT_MAPPING_VERSION = "v1";


    protected static final DateTimeFormatter INDEX_DATE_FORMAT_DAY = DateTimeFormat.forPattern("yyyy.MM.ww.DDD");
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_WEEK = DateTimeFormat.forPattern("yyyy.MM.ww");
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_MONTH = DateTimeFormat.forPattern("yyyy.MM");
    protected static final DateTimeFormatter INDEX_DATE_FORMAT_YEAR = DateTimeFormat.forPattern("yyyy");

    protected String mappingVersion;
    protected String indexPrefix;
    protected String indexPostFix;

    @Inject
    ConfigService configService;

    /** Cache that saves the annotation information of each searchable in regards to its index prefix */
    Map<Class<?>, String> indexNamePrefixes;

    /** Cache that saves the annotation information of each searchable in regards to its index period */
    Map<Class<?>, IndexPeriod> indexPeriods;

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
        Config config = configService.get(ElasticSearchConnectionRepository.ELASTIC_CONFIG);

        mappingVersion = config.get(MAPPING_VERSION_CONFIG).asString().orElse(DEFAULT_MAPPING_VERSION);

        String prefix = config.get(PROJECT_PREFIX_CONFIG).asString().orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(prefix)) {
            prefix = prefix.toLowerCase() + INDEX_SEPARATOR;
        }
        this.indexPrefix = prefix;
        String postfix = config.get(PROJECT_POSTFIX_CONFIG).asString().orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(postfix)) {
            postfix = INDEX_SEPARATOR + postfix.toLowerCase();
        }
        this.indexPostFix = postfix;
    }

    /**
     * Initializes the index names for each searchable. This is done only once at startup as it is defined by the
     * annotations on the searchable and cannot be changed at runtime.
     */
    @Inject
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

    protected String getIndexNamePrefix(Searchable searchable, boolean appendInfix ) {
        return (appendInfix  ? getIndexPrefix() : StringUtils.EMPTY) + searchable.getIndexName() + (appendInfix  ? getIndexPostfix() : StringUtils.EMPTY);
    }

    public String getIndexNamePrefixFromClass(Class<?> searchableClazz, boolean appendInfix ) {
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
        DateTimeFormatter formatter = getDateFormatter(indexPeriod);

        return getDateFormatString(new DateTime(searchable.getCreationDate()), formatter);
    }

    /**
     * Returns the appropriate date formatter for the given index period.
     */
    protected DateTimeFormatter getDateFormatter(IndexPeriod indexPeriod) {
        switch (indexPeriod) {
        case DAILY:
            return INDEX_DATE_FORMAT_DAY;
        case WEEKLY:
            return INDEX_DATE_FORMAT_WEEK;
        case MONTHLY:
            return INDEX_DATE_FORMAT_MONTH;
        case YEARLY:
            return INDEX_DATE_FORMAT_YEAR;
        case ALL:
            return null;
        case EXTERNAL:
            return null;
        case DEFAULT:
            return getDateFormatter(IndexPeriod.getDefault());
        default:
            return getDateFormatter(IndexPeriod.getDefault());
        }
    }

    /**
     * Formats the given {@link DateTime} with the given formatter. When the formatter is null the default value will be
     * returned.
     */
    protected String getDateFormatString(DateTime dateTime, DateTimeFormatter formatter) {
        if (formatter != null) {
            return formatter.print(dateTime);
        } else {
            return SEARCH_PROVIDER_NO_DATE_INDEX_POSTFIX;
        }
    }

}
