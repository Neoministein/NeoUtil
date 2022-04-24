package com.neo.javax.api.persitence;

import com.neo.common.impl.enumeration.Synchronization;

import java.io.Serializable;

/**
 * IndexParameter class to handle IndexParameter properties for the communication to the external search provider.
 */
public class IndexParameter implements Serializable {

    /**
     * The synchronization, defaults to ASYNCHRONOUS
     */
    private final Synchronization synchronization;

    public IndexParameter() {
        synchronization = Synchronization.ASYNCHRONOUS;
    }

    public IndexParameter(Synchronization synchronization) {
        this.synchronization = synchronization;
    }

    public Synchronization getSynchronization() {
        return synchronization;
    }
}