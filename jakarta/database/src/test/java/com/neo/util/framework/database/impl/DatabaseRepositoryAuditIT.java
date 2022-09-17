package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.RandomString;
import com.neo.util.common.impl.enumeration.PersistenceOperation;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.api.security.RolePrincipal;
import com.neo.util.framework.database.impl.entity.AddressEntity;
import com.neo.util.framework.database.impl.entity.PersonEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class DatabaseRepositoryAuditIT extends AbstractIntegrationTest {

    @Test
    void auditCreationTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        address.setZipcode(8001);
        subject.edit(address);

        subject.remove(address);

        EntityResult<EntityAuditTrail> entityResult = subject.find(new EntityQuery<>(EntityAuditTrail.class));
        List<EntityAuditTrail> result = entityResult.getHits();
        //Assert

        Assertions.assertEquals(3, entityResult.getHitCount());
        Assertions.assertEquals(PersistenceOperation.CREATE, result.get(0).getOperation());
        Assertions.assertEquals(address.getPrimaryKey().toString(), result.get(0).getObjectKey());
        Assertions.assertEquals(address.getClass().getSimpleName(), result.get(0).getClassType());

        Assertions.assertEquals(PersistenceOperation.UPDATE, result.get(1).getOperation());
        Assertions.assertEquals(address.getPrimaryKey().toString(), result.get(1).getObjectKey());
        Assertions.assertEquals(address.getClass().getSimpleName(), result.get(1).getClassType());

        Assertions.assertEquals(PersistenceOperation.DELETE, result.get(2).getOperation());
        Assertions.assertEquals(address.getPrimaryKey().toString(), result.get(2).getObjectKey());
        Assertions.assertEquals(address.getClass().getSimpleName(), result.get(2).getClassType());
    }

    @Test
    void transactionCountTest() {
        //Arrange
        PersonEntity person = new PersonEntity("Adelaide Fitzgerald",32,50.0, true);

        //Act
        subject.create(person);

        person.setAge(33);
        subject.edit(person);
        person.setAge(34);
        subject.edit(person);
        person.setAge(35);
        subject.edit(person);

        EntityResult<PersonEntity> entityResult = subject.find(new EntityQuery<>(PersonEntity.class));
        PersonEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(1, entityResult.getHitCount());
        Assertions.assertEquals(4, result.getTransactionCount());
    }

    @Test
    void requestIdFallOverTest() {
        //Arrange
        RequestDetailsDummy requestDetailsMock = weld.select(RequestDetailsDummy.class).get();
        String originalRequestId = requestDetailsMock.getRequestId();


        PersonEntity person = new PersonEntity("Amira Johns",32, 50.0, true);

        //Act
        subject.create(person);

        requestDetailsMock.setRequestId(new RandomString().nextString());
        person.setAge(33);
        subject.edit(person);


        EntityResult<PersonEntity> entityResult = subject.find(new EntityQuery<>(PersonEntity.class));
        PersonEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(originalRequestId, result.getCreatedBy());
        Assertions.assertEquals(requestDetailsMock.getRequestId(), result.getUpdatedBy());
        Assertions.assertTrue(result.getCreatedOn().before(result.getUpdatedOn()));
    }

    @Test
    void requestUserTest() {
        //Arrange
        String expectedUser = "ExpectedUserName";
        RequestDetailsDummy requestDetailsMock = weld.select(RequestDetailsDummy.class).get();
        requestDetailsMock.setUser(new RolePrincipal() {
            @Override
            public Set<String> getRoles() {
                return Set.of();
            }

            @Override
            public String getName() {
                return expectedUser;
            }
        });


        PersonEntity person = new PersonEntity("Jaylene Leach",32,50.0, true);

        //Act
        subject.create(person);
        person.setAge(33);
        subject.edit(person);


        EntityResult<PersonEntity> entityResult = subject.find(new EntityQuery<>(PersonEntity.class));
        PersonEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(expectedUser, result.getCreatedBy());
        Assertions.assertEquals(expectedUser, result.getUpdatedBy());
        Assertions.assertTrue(result.getCreatedOn().before(result.getUpdatedOn()));
    }
}
