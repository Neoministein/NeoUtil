package com.neo.util.framework.database.persistence;

import com.neo.util.framework.api.persistence.entity.PersistenceEntity;

import jakarta.persistence.*;

@Entity
@Table(name = EntityAuditTrail.TABLE_NAME)
@Cacheable(value = false)
public class EntityAuditTrail extends AuditableDataBaseEntity implements PersistenceEntity {

    public static final String TABLE_NAME = "audit_trail";

    public static final String C_OBJECT_KEY = "object_key";
    public static final String C_CLASS_TYPE = "class_type";
    public static final String C_OPERATION = "operation";

    @Id
    @Column(name = PersistenceEntity.C_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;

    @Column(name = C_OBJECT_KEY, nullable = false)
    protected String objectKey;

    @Column(name = C_CLASS_TYPE, nullable = false)
    protected String classType;

    @Column(name = C_OPERATION, nullable = false)
    protected String operation;

    protected EntityAuditTrail() {
        //Required by JPA
    }

    public EntityAuditTrail(String objectKey, String classType, String operation) {
        this.objectKey = objectKey;
        this.classType = classType;
        this.operation = operation;
    }

    @PreUpdate
    private void preUpdate() {
        throw new IllegalArgumentException("Updating an audit entity is not supported");
    }

    public Long getId() {
        return id;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String classKey) {
        this.objectKey = classKey;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public Object getPrimaryKey() {
        return getId();
    }
}
