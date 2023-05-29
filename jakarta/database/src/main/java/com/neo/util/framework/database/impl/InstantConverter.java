package com.neo.util.framework.database.impl;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.util.Date;

/**
 * A converter that converts a {@link Instant} to {@link Date} for usages in a database
 */
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Date> {

    @Override
    public Date convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : Date.from(attribute);
    }

    @Override
    public Instant convertToEntityAttribute(Date dbData) {
        return dbData == null ? null : dbData.toInstant();
    }
}