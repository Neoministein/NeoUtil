package com.neo.util.helidon.rest.entity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class JqplResource {


    @Inject
    JqplTransaction jqplTransaction;

    public void save(TestPersonEntity testPersonEntity) {
        jqplTransaction.save(testPersonEntity);
    }

    public List<TestPersonEntity> findAll() {
        return jqplTransaction.findAll();
    }

    public List<TestPersonEntity> findAllJPQL() {
        return jqplTransaction.findAllJPQL();
    }
}
