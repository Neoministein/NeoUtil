package com.neo.util.framework.database.impl;

import com.neo.util.common.impl.ThreadUtils;
import com.neo.util.common.impl.enumeration.Association;
import com.neo.util.framework.api.persistence.criteria.*;
import com.neo.util.framework.api.persistence.entity.EntityQuery;
import com.neo.util.framework.api.persistence.entity.EntityResult;
import com.neo.util.framework.database.impl.entity.PersonEntity;
import com.neo.util.framework.database.persistence.AuditableDataBaseEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

class DatabaseProviderFindIT extends AbstractIntegrationTest<DatabaseProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProviderFindIT.class);

    @Override
    protected Class<DatabaseProvider> getSubjectClass() {
        return DatabaseProvider.class;
    }

    PersonEntity personOne;
    PersonEntity personTwo;
    PersonEntity personThree;
    PersonEntity personFour;

    /*
     * Normally Thread.sleep should be avoided however we require at least 1 millisecond of delay between the creation
     * of the entity for the dateRangeSearchTest, since on faster machines it can happen that multiple entities get
     * created at the same point in time
     */
    @BeforeEach
    void setupEntity() throws InterruptedException {
        personOne = new PersonEntity("Heaven Schneider",10,40.0,false);
        personTwo = new PersonEntity("Catherine Leon",20,45.0, false);
        personThree = new PersonEntity("Davian Chang",30,50.0,false);
        personFour = new PersonEntity("Gabriel Ryan",40,55.0,true);

        subject.create(personOne);
        ThreadUtils.simpleSleep(1);
        subject.create(personTwo);
        ThreadUtils.simpleSleep(1);
        subject.create(personThree);
        ThreadUtils.simpleSleep(1);
        subject.create(personFour);
    }

    @Test
    void longRangeSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> fromQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new LongRangeSearchCriteria(PersonEntity.C_AGE, 15L,null)
        ));
        EntityQuery<PersonEntity> toQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new LongRangeSearchCriteria(PersonEntity.C_AGE, null,35L)
        ));
        EntityQuery<PersonEntity> betweenQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new LongRangeSearchCriteria(PersonEntity.C_AGE, 15L,35L)
        ));
        //Act

        EntityResult<PersonEntity> fromResult = subject.fetch(fromQuery);
        EntityResult<PersonEntity> toResult = subject.fetch(toQuery);
        EntityResult<PersonEntity> betweenResult = subject.fetch(betweenQuery);
        //Assert

        Assertions.assertEquals(3, fromResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), fromResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), fromResult.getHits().get(1).getId());
        Assertions.assertEquals(personFour.getId(), fromResult.getHits().get(2).getId());

        Assertions.assertEquals(3, toResult.getHitSize());
        Assertions.assertEquals(personOne.getId(), toResult.getHits().get(0).getId());
        Assertions.assertEquals(personTwo.getId(), toResult.getHits().get(1).getId());
        Assertions.assertEquals(personThree.getId(), toResult.getHits().get(2).getId());

        Assertions.assertEquals(2, betweenResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), betweenResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), betweenResult.getHits().get(1).getId());
    }

    @Test
    void doubleRangeSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> fromQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new DoubleRangeSearchCriteria(PersonEntity.C_WEIGHT, 42.5,null)
        ));
        EntityQuery<PersonEntity> toQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new DoubleRangeSearchCriteria(PersonEntity.C_WEIGHT, null,42.5)
        ));
        EntityQuery<PersonEntity> betweenQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new DoubleRangeSearchCriteria(PersonEntity.C_WEIGHT, 42.5,52.5)
        ));
        //Act

        EntityResult<PersonEntity> fromResult = subject.fetch(fromQuery);
        EntityResult<PersonEntity> toResult = subject.fetch(toQuery);
        EntityResult<PersonEntity> betweenResult = subject.fetch(betweenQuery);
        //Assert

        Assertions.assertEquals(3, fromResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), fromResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), fromResult.getHits().get(1).getId());
        Assertions.assertEquals(personFour.getId(), fromResult.getHits().get(2).getId());

        Assertions.assertEquals(1, toResult.getHitSize());
        Assertions.assertEquals(personOne.getId(), toResult.getHits().get(0).getId());

        Assertions.assertEquals(2, betweenResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), betweenResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), betweenResult.getHits().get(1).getId());
    }

    @Test
    void explicitBooleanSearchSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> entityQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new ExplicitSearchCriteria(PersonEntity.C_TWO_ARMS, true)
        ));
        //Act

        EntityResult<PersonEntity> result = subject.fetch(entityQuery);
        List<PersonEntity> resultList = result.getHits();
        //Assert

        Assertions.assertEquals(1, result.getHitSize());
        Assertions.assertEquals(resultList.get(0).getId(), personFour.getId());
    }

    @Test
    void explicitIntegerSearchSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> entityQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new ExplicitSearchCriteria(PersonEntity.C_AGE, 20)
        ));
        //Act

        EntityResult<PersonEntity> result = subject.fetch(entityQuery);
        List<PersonEntity> resultList = result.getHits();
        //Assert

        Assertions.assertEquals(1, result.getHitSize());
        Assertions.assertEquals(resultList.get(0).getId(), personTwo.getId());
    }

    @Test
    void explicitWildcardSearchTest() {
        //Arrange

        EntityQuery<PersonEntity> starQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new ExplicitSearchCriteria(PersonEntity.C_NAME, "*Leon", true)
        ));

        EntityQuery<PersonEntity> questionQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new ExplicitSearchCriteria(PersonEntity.C_NAME, "Heaven Schne?der", true)
        ));
        //Act

        EntityResult<PersonEntity> starResult = subject.fetch(starQuery);
        EntityResult<PersonEntity> questionResult = subject.fetch(questionQuery);
        //Assert

        Assertions.assertEquals(1, starResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), starResult.getHits().get(0).getId());

        Assertions.assertEquals(1, questionResult.getHitSize());
        Assertions.assertEquals(personOne.getId() ,questionResult.getHits().get(0).getId());
    }

    @Test
    void explicitStringSearchSearchTest() {
        //Arrange

        EntityQuery<PersonEntity> entityQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new ExplicitSearchCriteria(PersonEntity.C_NAME, "Catherine Leon")
        ));
        //Act

        EntityResult<PersonEntity> result = subject.fetch(entityQuery);
        List<PersonEntity> resultList = result.getHits();
        //Assert

        Assertions.assertEquals(1, result.getHitSize());
        Assertions.assertEquals(resultList.get(0).getId(), personTwo.getId());
    }

    @Test
    void existingSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> query = new EntityQuery<>(PersonEntity.class, List.of(
                new ExistingFieldSearchCriteria(PersonEntity.C_AGE)
        ));

        personThree.setAge(null);
        subject.edit(personThree);
        //Act
        EntityResult<PersonEntity> result = subject.fetch(query);

        //Assert
        Assertions.assertEquals(3, result.getHitSize());
        Assertions.assertEquals(personOne.getId(), result.getHits().get(0).getId());
        Assertions.assertEquals(personTwo.getId(), result.getHits().get(1).getId());
        Assertions.assertEquals(personFour.getId(), result.getHits().get(2).getId());
    }

    @Test
    void dateRangeSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> fromQuery = new EntityQuery<>(PersonEntity.class, List.of(
               new DateSearchCriteria(AuditableDataBaseEntity.C_CREATED_ON, personTwo.getCreatedOn(), null)));
        EntityQuery<PersonEntity> toQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new DateSearchCriteria(AuditableDataBaseEntity.C_CREATED_ON, null, personTwo.getCreatedOn())));
        EntityQuery<PersonEntity> betweenQuery = new EntityQuery<>(PersonEntity.class, List.of(
                new DateSearchCriteria(AuditableDataBaseEntity.C_CREATED_ON, personTwo.getCreatedOn(), personThree.getCreatedOn())));

        //Act
        EntityResult<PersonEntity> fromResult = subject.fetch(fromQuery);
        EntityResult<PersonEntity> toResult = subject.fetch(toQuery);
        EntityResult<PersonEntity> betweenResult = subject.fetch(betweenQuery);
        //Assert

        Assertions.assertEquals(3,fromResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), fromResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), fromResult.getHits().get(1).getId());
        Assertions.assertEquals(personFour.getId(), fromResult.getHits().get(2).getId());

        Assertions.assertEquals(2, toResult.getHitSize());
        Assertions.assertEquals(personOne.getId(), toResult.getHits().get(0).getId());
        Assertions.assertEquals(personTwo.getId(), toResult.getHits().get(1).getId());

        Assertions.assertEquals(2,betweenResult.getHitSize());
        Assertions.assertEquals(personTwo.getId(), betweenResult.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), betweenResult.getHits().get(1).getId());
    }

    @Test
    void containsSearchTest() {
        //Arrange
        EntityQuery<PersonEntity> query = new EntityQuery<>(PersonEntity.class, List.of(
                new ContainsSearchCriteria(PersonEntity.C_AGE, 0, 10,40)));
        //Act
        EntityResult<PersonEntity> result = subject.fetch(query);

        //Assert
        Assertions.assertEquals(2,result.getHitSize());
        Assertions.assertEquals(personOne.getId(), result.getHits().get(0).getId());
        Assertions.assertEquals(personFour.getId(), result.getHits().get(1).getId());
    }

    @Test
    void combinedSearchAndTest() {
        //Arrange
        EntityQuery<PersonEntity> query = new EntityQuery<>(PersonEntity.class, List.of(
                new CombinedSearchCriteria(
                        new ContainsSearchCriteria(PersonEntity.C_AGE, 0, 10,40),
                        new ExplicitSearchCriteria(PersonEntity.C_NAME, "*ne*", true))));
        //Act
        EntityResult<PersonEntity> result = subject.fetch(query);

        //Assert
        Assertions.assertEquals(1, result.getHitSize());
        Assertions.assertEquals(personOne.getId(), result.getHits().get(0).getId());
    }

    @Test
    void combinedSearchOrTest() {
        //Arrange
        EntityQuery<PersonEntity> query = new EntityQuery<>(PersonEntity.class, List.of(
                new CombinedSearchCriteria(Association.OR,
                        new ContainsSearchCriteria(PersonEntity.C_AGE, 0, 10,40),
                        new ExplicitSearchCriteria(PersonEntity.C_NAME, "*ne*", true))));
        //Act
        EntityResult<PersonEntity> result = subject.fetch(query);

        //Assert
        Assertions.assertEquals(3, result.getHitSize());
        Assertions.assertEquals(personOne.getId(), result.getHits().get(0).getId());
        Assertions.assertEquals(personTwo.getId(), result.getHits().get(1).getId());
        Assertions.assertEquals(personFour.getId(), result.getHits().get(2).getId());
    }

    @Test
    void orderTest() {
        //Arrange
        int maxResult = 2;

        EntityQuery<PersonEntity> query = new EntityQuery<>(PersonEntity.class, 0, maxResult, List.of(), Map.of(
                PersonEntity.C_WEIGHT, false
        ));
        //Act
        EntityResult<PersonEntity> result = subject.fetch(query);

        //Assert
        Assertions.assertEquals(maxResult, result.getHitCount());
        Assertions.assertEquals(4, result.getHitSize());
        Assertions.assertEquals(personFour.getId(), result.getHits().get(0).getId());
        Assertions.assertEquals(personThree.getId(), result.getHits().get(1).getId());
    }
}
