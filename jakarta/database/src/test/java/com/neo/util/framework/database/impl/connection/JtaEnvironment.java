package com.neo.util.framework.database.impl.connection;

import com.arjuna.ats.jta.utils.JNDIManager;
import org.jnp.server.NamingBeanImpl;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JtaEnvironment implements BeforeEachCallback, AfterEachCallback {

    private NamingBeanImpl namingBean;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        namingBean = new NamingBeanImpl();
        namingBean.start();

        JNDIManager.bindJTAImplementation();
        TransactionalConnectionProvider.bindDataSource();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        namingBean.stop();
    }
}