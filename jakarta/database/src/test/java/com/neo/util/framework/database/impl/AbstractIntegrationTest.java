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
abstract class AbstractIntegrationTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            DatabaseProvider.class,
            M2PersistenceContextService.class,
            RequestDetailsDummy.class,
            TransactionExtension.class
    ).activate(RequestScoped.class).build();

    protected DatabaseProvider subject;

    @BeforeEach
    void init() {
        subject = weld.select(DatabaseProvider.class).get();
    }
}
