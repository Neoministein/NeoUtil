package com.neo.util.framework.database.impl;

import com.neo.util.framework.database.impl.connection.JtaEnvironment;
import com.neo.util.framework.impl.connection.RequestDetailsProducer;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JtaEnvironment.class)
@ExtendWith(WeldJunit5Extension.class)
public abstract class AbstractIntegrationTest<T> {

    @WeldSetup
    protected WeldInitiator weld = WeldInitiator.from(new Weld()).activate(RequestScoped.class).build();

    protected abstract Class<T> getSubjectClass();

    protected T subject;

    @BeforeEach
    void init() {
        weld.select(RequestDetailsProducer.class).get().setRequestDetails(new RequestDetailsDummy());

        subject = weld.select(getSubjectClass()).get();
    }
}
