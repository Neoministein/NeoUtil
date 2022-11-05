package com.neo.util.helidon.rest.entity;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = TestLimbEntity.TABLE_NAME)
public class TestLimbEntity implements PersistenceEntity {

    public static final String TABLE_NAME = "limb";

    public static final String C_LIMB = "limb";

    @Id
    @Column(name = PersistenceEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = C_LIMB)
    private String limb;

    public TestLimbEntity() {}

    public TestLimbEntity(String limb) {
        this.limb = limb;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLimb() {
        return limb;
    }

    public void setLimb(String limb) {
        this.limb = limb;
    }

    @Override
    public Object getPrimaryKey() {
        return getId();
    }
}
