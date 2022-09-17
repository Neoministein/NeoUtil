package com.neo.util.framework.startup.impl;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ListenerSequenceRecorder {

    protected List<Class<?>> callSequence = new ArrayList<>();

    public void addToSequence(Class<?> clazz) {
        callSequence.add(clazz);
    }

    public List<Class<?>> getCallSequence() {
        return callSequence;
    }
}
