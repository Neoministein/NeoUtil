package com.neo.util.helidon.rest.entity;

import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

@AddBean(JqplResource.class)
@AddBean(JqplTransaction.class)
@HelidonTest
class JpqlIT extends AbstractIntegrationTest {

    @Inject
    JqplResource resource;

    @BeforeEach
    void init() {
        TestLimbEntity armLeft = new TestLimbEntity("Arm Left");
        TestLimbEntity armRight = new TestLimbEntity("Arm Right");
        TestLimbEntity legLeft = new TestLimbEntity("Leg Left");
        TestLimbEntity legRight = new TestLimbEntity("Arm Right");

        TestPersonEntity testPersonEntity = new TestPersonEntity();
        testPersonEntity.setTestLimbEntities(List.of(armLeft,armRight,legLeft,legRight));
        testPersonEntity.setName("AName");
        testPersonEntity.setAge(22);
        testPersonEntity.setDescription("A Description");
        resource.save(testPersonEntity);
    }

    @Test
    void testStuff() {
        List<TestPersonEntity> testPersonEntities = resource.findAll();
        System.out.println(testPersonEntities);

        List<TestPersonEntity> allJPQL = resource.findAllJPQL();
        System.out.println(testPersonEntities);
    }
}
