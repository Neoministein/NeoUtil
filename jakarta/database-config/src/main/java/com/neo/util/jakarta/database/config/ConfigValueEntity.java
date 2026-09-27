package com.neo.util.jakarta.database.config;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.util.common.api.json.Views;
import com.neo.util.jakarta.database.audit.AuditableDataBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "CONFIG_VALUE")
public class ConfigValueEntity extends AuditableDataBaseEntity<String> {

    @Id
    @Column(name = "KEY", nullable = false)
    @JsonView(Views.Admin.class)
    private String key;

    @Column(name = "VALUE", nullable = false)
    private String value;

    public ConfigValueEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    protected ConfigValueEntity() {
        // Required by JPA
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConfigValueEntity that = (ConfigValueEntity) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public String getPrimaryKey() {
        return key;
    }
}
