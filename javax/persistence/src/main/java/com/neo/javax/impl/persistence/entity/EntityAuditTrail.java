package com.neo.javax.impl.persistence.entity;

import com.neo.common.impl.enumeration.PersistenceOperation;
import com.neo.common.impl.exception.InternalLogicException;
import com.neo.javax.api.persitence.entity.DataBaseEntity;

import javax.persistence.*;

@Entity
@Table(name = EntityAuditTrail.TABLE_NAME)
@Cacheable(value = false)
public class EntityAuditTrail extends AbstractDataBaseEntity implements DataBaseEntity {

    public static final String TABLE_NAME = "AuditTrail";

    public static final String C_OBJECT_KEY = "cobject_key";
    public static final String C_CLASS_TYPE = "class_type";
    public static final String C_OPERATION = "operation";

    @Id
    @Column(name = DataBaseEntity.C_ID, columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = C_OBJECT_KEY)
    private String objectKey;

    @Column(name = C_CLASS_TYPE)
    private String classType;

    @Column(name = C_OPERATION)
    private PersistenceOperation operation;

    @PreUpdate
    private void preUpdate() {
        throw new InternalLogicException("Updating an audit entity is not supported");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public PersistenceOperation getOperation() {
        return operation;
    }

    public void setOperation(PersistenceOperation operation) {
        this.operation = operation;
    }

    @Override
    public Object getPrimaryKey() {
        return getId();
    }
}
