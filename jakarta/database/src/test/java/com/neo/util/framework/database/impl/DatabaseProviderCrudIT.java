package com.neo.util.framework.database.impl;

import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.database.impl.entity.AddressEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class DatabaseProviderCrudIT extends AbstractIntegrationTest {

    @Test
    void createOneTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        EntityResult<AddressEntity> entityResult = subject.fetch(new EntityQuery<>(AddressEntity.class));
        AddressEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(1, entityResult.getHitCount());

        Assertions.assertEquals(address.getCity(), result.getCity());
        Assertions.assertEquals(address.getZipcode(), result.getZipcode());
    }

    @Test
    void createMultipleTest() {
        //Arrange
        AddressEntity addressZurich = new AddressEntity("Zurich",8000);
        AddressEntity addressBern = new AddressEntity("Bern",8000);

        //Act
        subject.create(addressZurich);
        subject.create(addressBern);

        EntityResult<AddressEntity> entityResult = subject.fetch(new EntityQuery<>(AddressEntity.class));
        List<AddressEntity> result = entityResult.getHits();
        //Assert

        Assertions.assertEquals(2, entityResult.getHitCount());
    }

    @Test
    void findByPrimaryKey() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        Optional<AddressEntity> entityResult = subject.fetch(address.getId(), AddressEntity.class);
        //Assert

        Assertions.assertTrue(entityResult.isPresent());
        AddressEntity result = entityResult.get();

        Assertions.assertEquals(address.getCity(), result.getCity());
        Assertions.assertEquals(address.getZipcode(), result.getZipcode());
    }

    @Test
    void findNonByPrimaryKey() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        Optional<AddressEntity> wrongPrimaryKey = subject.fetch(200, AddressEntity.class);
        //Assert

        Assertions.assertFalse(wrongPrimaryKey.isPresent());
    }

    @Test
    void updateTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);
        address.setZipcode(8001);
        subject.edit(address);

        EntityResult<AddressEntity> entityResult = subject.fetch(new EntityQuery<>(AddressEntity.class));
        AddressEntity result = entityResult.getFirst().get();
        //Assert

        Assertions.assertEquals(1, entityResult.getHitCount());

        Assertions.assertEquals(8001, result.getZipcode());
    }

    @Test
    void deleteTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);
        subject.remove(address);

        EntityResult<AddressEntity> entityResult = subject.fetch(new EntityQuery<>(AddressEntity.class));
        //Assert

        Assertions.assertEquals(0, entityResult.getHitCount());
    }

    @Test
    void countTest() {
        //Arrange
        AddressEntity addressZurich = new AddressEntity("Zurich",8000);
        AddressEntity addressBern = new AddressEntity("Bern",8001);
        AddressEntity addressBasel = new AddressEntity("Basel", 8002);

        //Act
        subject.create(addressZurich);
        subject.create(addressBern);
        subject.create(addressBasel);

        long countResult = subject.count(List.of(), AddressEntity.class);
        //Assert

        Assertions.assertEquals(3, countResult);
    }
}
