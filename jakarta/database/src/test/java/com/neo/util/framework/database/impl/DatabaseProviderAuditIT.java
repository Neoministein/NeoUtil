package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.database.impl.entity.PersonEntity;
import com.neo.util.framework.impl.request.DummyRequestDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DatabaseProviderAuditIT extends AbstractIntegrationTest<DatabaseProvider> {

    @Override
    protected Class<DatabaseProvider> getSubjectClass() {
        return DatabaseProvider.class;
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

        EntityResult<PersonEntity> entityResult = subject.fetch(new EntityQuery<>(PersonEntity.class));
        PersonEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(1, entityResult.getHitCount());
        Assertions.assertEquals(4, result.getTransactionCount());
    }

    @Test
    void requestUserTest() {
        //Arrange
        String expectedUser = "ExpectedUserName";
        DummyRequestDetails requestDetailsMock = (DummyRequestDetails) weld.select(RequestDetails.class).get();
        requestDetailsMock.setCaller(expectedUser);


        PersonEntity person = new PersonEntity("Jaylene Leach",32,50.0, true);

        //Act
        subject.create(person);
        person.setAge(33);
        subject.edit(person);


        EntityResult<PersonEntity> entityResult = subject.fetch(new EntityQuery<>(PersonEntity.class));
        PersonEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(expectedUser, result.getCreatedBy());
        Assertions.assertEquals(expectedUser, result.getUpdatedBy());
        Assertions.assertTrue(result.getCreatedOn().isBefore(result.getUpdatedOn()));
    }
}
