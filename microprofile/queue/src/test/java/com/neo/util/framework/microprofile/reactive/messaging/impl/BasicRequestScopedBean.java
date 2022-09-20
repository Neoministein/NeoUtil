package com.neo.util.framework.microprofile.reactive.messaging.impl;

import jakarta.enterprise.context.RequestScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class BasicRequestScopedBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRequestScopedBean.class);

    public void triggerClassMethod() {
        LOGGER.info("The request scoped bean has been successfully triggered");
    }
}
