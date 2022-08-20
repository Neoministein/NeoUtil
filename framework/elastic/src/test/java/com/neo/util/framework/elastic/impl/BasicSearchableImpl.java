package com.neo.util.framework.elastic.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.search.GenericSearchable;
import com.neo.util.framework.api.persistence.search.IndexPeriod;

public class BasicSearchableImpl extends GenericSearchable {

    public static final String INDEX_NAME = "basicindex";

    public static final String FIELD_NAME_TEXT_FIELD = "textField";
    public static final String TEXT_FIELD_VALUE = "textValue";

    private String textField = TEXT_FIELD_VALUE;
    private int intField = 123;
    private Long longObjField = 1234567890L;

    public BasicSearchableImpl() {
        setVersion(10L);
    }

    @Override
    public String getIndexName() {
        return INDEX_NAME;
    }

    @Override
    public IndexPeriod getIndexPeriod() {
        return IndexPeriod.ALL;
    }

    @Override
    public ObjectNode getJsonNode() {
        return JsonUtil.fromPojo(this);
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public Long getLongObjField() {
        return longObjField;
    }

    public void setLongObjField(Long longObjField) {
        this.longObjField = longObjField;
    }

}
