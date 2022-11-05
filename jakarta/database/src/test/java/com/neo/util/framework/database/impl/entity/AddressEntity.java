package com.neo.util.framework.database.impl.entity;

import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = AddressEntity.TABLE_NAME)
public class AddressEntity implements PersistenceEntity {

    public static final String TABLE_NAME = "address";
    public static final String C_CITY = "city";
    public static final String C_ZIP_CODE = "zipcode";

    @Id
    @Column(name = PersistenceEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = C_CITY)
    protected String city;

    @Column(name = C_ZIP_CODE)
    protected Integer zipcode;


    @ManyToOne
    private PersonEntity person;

    public AddressEntity(String city, Integer zipcode) {
        this.city = city;
        this.zipcode = zipcode;
    }

    public AddressEntity() {
        //Required by JPA
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public Integer getZipcode() {
        return zipcode;
    }

    public void setZipcode(Integer zipcode) {
        this.zipcode = zipcode;
    }

    @Override
    public Object getPrimaryKey() {
        return id;
    }

    @Override
    public String toString() {
        return "\"" + this.getClass().getSimpleName() + "\":" + JsonUtil.toJson(this);
    }
}
