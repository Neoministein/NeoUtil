package com.neo.util.framework.websocket;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;

import java.io.Serializable;

@SessionScoped
public class TestBean implements Serializable {


    public TestBean() {
        System.out.println("A");
    }

    public void aMethod() {
        System.out.println("B");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("C");
    }
}
