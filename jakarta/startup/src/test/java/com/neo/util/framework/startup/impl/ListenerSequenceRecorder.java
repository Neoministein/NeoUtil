package com.neo.util.framework.startup.impl;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ListenerSequenceRecorder {

    protected List<String> callSequence = new ArrayList<>();

    public void addToSequence(String eventName) {
        callSequence.add(eventName);
    }

    public List<String> getCallSequence() {
        return callSequence;
    }
}
