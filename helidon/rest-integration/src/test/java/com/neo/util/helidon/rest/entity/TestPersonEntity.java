package com.neo.util.helidon.rest.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.neo.util.common.api.json.Views;
import com.neo.util.framework.api.persistence.entity.DataBaseEntity;

import javax.persistence.*;

@Entity
@Table(name = TestPersonEntity.TABLE_NAME)
public class TestPersonEntity implements DataBaseEntity {

    public static final String TABLE_NAME = "testEntity";

    public static final String C_TEXT = "name";
    public static final String C_DESCRIPTION = "description";
    public static final String C_AGE = "age";

    @Id
    @Column(name = DataBaseEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = C_TEXT, nullable = false, unique = true)
    private String name;

    @Column(name = C_DESCRIPTION)
    private String description;

    @Column(name = C_AGE)
    private Integer age;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public Object getPrimaryKey() {
        return getId();
    }
}
