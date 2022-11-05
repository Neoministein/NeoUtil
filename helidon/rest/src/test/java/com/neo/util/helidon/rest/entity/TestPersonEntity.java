package com.neo.util.helidon.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = TestPersonEntity.TABLE_NAME)
public class TestPersonEntity implements PersistenceEntity {

    public static final String TABLE_NAME = "testEntity";

    public static final String C_TEXT = "name";
    public static final String C_DESCRIPTION = "description";
    public static final String C_AGE = "age";

    @Id
    @Column(name = PersistenceEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = C_TEXT, nullable = false, unique = true)
    private String name;

    @Column(name = C_DESCRIPTION)
    private String description;

    @Column(name = C_AGE)
    private Integer age;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TestLimbEntity> testLimbEntities;

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

    public List<TestLimbEntity> getTestLimbEntities() {
        return testLimbEntities;
    }

    public void setTestLimbEntities(List<TestLimbEntity> testLimbEntities) {
        this.testLimbEntities = testLimbEntities;
    }

    @Override
    public Object getPrimaryKey() {
        return getId();
    }
}
