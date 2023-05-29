package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.enumeration.PersistenceOperation;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.database.impl.entity.PersonEntity;
import com.neo.util.framework.impl.RequestContextExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class DatabaseProviderAuditIT extends AbstractIntegrationTest<DatabaseProvider> {

    @Override
    protected Class<DatabaseProvider> getSubjectClass() {
        return DatabaseProvider.class;
    }

    @Test
    void auditCreationTest() {
        //Arrange
        PersonEntity person = new PersonEntity("Jaylene Leach",32,50.0, true);

        //Act
        weld.select(RequestContextExecutor.class).get().execute(new RequestDetailsDummy(), () -> subject.create(person));


        person.setAge(33);
        subject.edit(person);

        subject.remove(person);

        EntityResult<EntityAuditTrail> entityResult = subject.fetch(new EntityQuery<>(EntityAuditTrail.class));
        List<EntityAuditTrail> result = entityResult.getHits();
        //Assert

        Assertions.assertEquals(3, entityResult.getHitCount());
        Assertions.assertEquals(PersistenceOperation.CREATE.toString(), result.get(0).getOperation());
        Assertions.assertEquals(person.getPrimaryKey().toString(), result.get(0).getObjectKey());
        Assertions.assertEquals(person.getClass().getSimpleName(), result.get(0).getClassType());

        Assertions.assertEquals(PersistenceOperation.UPDATE.toString(), result.get(1).getOperation());
        Assertions.assertEquals(person.getPrimaryKey().toString(), result.get(1).getObjectKey());
        Assertions.assertEquals(person.getClass().getSimpleName(), result.get(1).getClassType());

        Assertions.assertEquals(PersistenceOperation.DELETE.toString(), result.get(2).getOperation());
        Assertions.assertEquals(person.getPrimaryKey().toString(), result.get(2).getObjectKey());
        Assertions.assertEquals(person.getClass().getSimpleName(), result.get(2).getClassType());
    }
}
