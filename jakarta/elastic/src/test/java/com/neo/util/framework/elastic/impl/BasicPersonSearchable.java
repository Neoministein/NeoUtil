package com.neo.util.framework.elastic.impl;

import com.neo.util.framework.api.persistence.search.AbstractSearchable;
import com.neo.util.framework.api.persistence.search.IndexPeriod;
import com.neo.util.framework.api.persistence.search.SearchableIndex;

import java.time.Instant;

@SearchableIndex(indexName = BasicPersonSearchable.INDEX_NAME, indexPeriod = IndexPeriod.ALL)
public class BasicPersonSearchable extends AbstractSearchable {

    public static final String INDEX_NAME = "basicindex";

    public static final String F_NAME = "name";
    public static final String F_AGE = "age";
    public static final String F_WEIGHT = "weight";
    public static final String F_HAS_TWO_ARMS = "hasTwoArms";
    public static final String F_TIMESTAMP = "timestamp";
    public static final String NAME_VALUE = "Steven";

    private String name = NAME_VALUE;
    private int age = 20;
    private Double weight = 45.5;
    private Boolean hasTwoArms = false;
    private Instant timestamp = Instant.now();

    public BasicPersonSearchable() {
        setVersion(10L);
    }

    public BasicPersonSearchable(String uuid, String name, int age, Double height, Boolean hasTwoArms) {
        this();
        setBusinessId(uuid);
        this.name = name;
        this.age = age;
        this.weight = height;
        this.hasTwoArms = hasTwoArms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean isHasTwoArms() {
        return hasTwoArms;
    }

    public void setHasTwoArms(Boolean hasTwoArms) {
        this.hasTwoArms = hasTwoArms;
    }

    public Boolean getHasTwoArms() {
        return hasTwoArms;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
