package com.neo.util.helion.integreation;

import com.neo.util.helion.integreation.connection.DefaultPersistenceContext;
import io.helidon.microprofile.tests.junit5.AddBean;


@AddBean( value = DefaultPersistenceContext.class)
public abstract class AbstractIntegrationTest {
}
