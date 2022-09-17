package com.neo.util.framework.database.impl.entity;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.database.impl.AuditableDataBaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = PersonEntity.TABLE_NAME)
public class PersonEntity extends AuditableDataBaseEntity implements PersistenceEntity {

    public static final String TABLE_NAME = "person";

    public static final String C_NAME = "name";
    public static final String C_AGE = "age";
    public static final String C_WEIGHT = "weight";
    public static final String C_TWO_ARMS = "hasTwoArms";

    @Id
    @Column(name = PersistenceEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = C_NAME, nullable = false)
    protected String name;

    @Column(name = C_AGE, nullable = true)
    protected Integer age;

    @Column(name = C_WEIGHT, nullable = true)
    protected Double weight;

    @Column(name = C_TWO_ARMS, nullable = false)
    protected boolean hasTwoArms;

    @OneToMany(mappedBy = TABLE_NAME, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AddressEntity> address = new ArrayList<>();

    public PersonEntity(String name, Integer age, Double weight, boolean hasTwoArms) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.hasTwoArms = hasTwoArms;
    }

    public PersonEntity() {
        //Required by JPA and Jackson
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isHasTwoArms() {
        return hasTwoArms;
    }

    public void setHasTwoArms(boolean hasTwoArms) {
        this.hasTwoArms = hasTwoArms;
    }

    public List<AddressEntity> getAddress() {
        return address;
    }

    public void setAddress(List<AddressEntity> address) {
        this.address = address;
    }

    @Override
    public Object getPrimaryKey() {
        return id;
    }
}
