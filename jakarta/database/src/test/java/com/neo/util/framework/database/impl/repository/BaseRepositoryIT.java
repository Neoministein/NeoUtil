package com.neo.util.framework.database.impl.repository;

import com.neo.util.framework.database.impl.AbstractIntegrationTest;
import com.neo.util.framework.database.impl.entity.AddressEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class BaseRepositoryIT extends AbstractIntegrationTest<AddressRepository> {

    @Override
    protected Class<AddressRepository> getSubjectClass() {
        return AddressRepository.class;
    }

    @Test
    void createOneTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        List<AddressEntity> entityResult = subject.fetchAll();
        //Assert

        Assertions.assertEquals(1, entityResult.size());

        Assertions.assertEquals(address.getCity(), entityResult.get(0).getCity());
        Assertions.assertEquals(address.getZipcode(), entityResult.get(0).getZipcode());
    }

    @Test
    void createMultipleTest() {
        //Arrange
        AddressEntity addressZurich = new AddressEntity("Zurich",8000);
        AddressEntity addressBern = new AddressEntity("Bern",8000);

        //Act
        subject.create(addressZurich);
        subject.create(addressBern);

        List<AddressEntity> entityResult = subject.fetchAll();
        //Assert

        Assertions.assertEquals(2, entityResult.size());
    }

    @Test
    void findByPrimaryKey() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);

        Optional<AddressEntity> entityResult = subject.fetch(address.getId());
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

        Optional<AddressEntity> wrongPrimaryKey = subject.fetch(200);
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

        List<AddressEntity> entityResult = subject.fetchAll();
        //Assert

        Assertions.assertEquals(1, entityResult.size());

        Assertions.assertEquals(8001, entityResult.get(0).getZipcode());
    }

    @Test
    void deleteTest() {
        //Arrange
        AddressEntity address = new AddressEntity("Zurich",8000);

        //Act
        subject.create(address);
        subject.remove(address);

        List<AddressEntity> entityResult = subject.fetchAll();
        //Assert

        Assertions.assertEquals(0, entityResult.size());
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

        long countResult = subject.count();
        //Assert

        Assertions.assertEquals(3, countResult);
    }
}
