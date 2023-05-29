package com.neo.util.framework.database.impl;

import com.arjuna.ats.jta.cdi.TransactionExtension;
import com.neo.util.framework.database.impl.connection.JtaEnvironment;
import jakarta.enterprise.context.RequestScoped;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JtaEnvironment.class)
@ExtendWith(WeldJunit5Extension.class)
public abstract class AbstractIntegrationTest<T> {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            M2PersistenceContextProvider.class,
            RequestDetailsDummy.class,
            TransactionExtension.class,
            getSubjectClass()
    ).activate(RequestScoped.class).build();

    protected abstract Class<T> getSubjectClass();

    protected T subject;

    @BeforeEach
    void init() {
        subject = weld.select(getSubjectClass()).get();
    }
}
