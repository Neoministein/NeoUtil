package com.neo.util.framework.jobrunr.impl;

import com.neo.util.framework.jobrunr.api.JobRunrStorageProvider;
import jakarta.enterprise.context.Dependent;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;

@Dependent
public class JobRunnerInMemoryStorageProvider implements JobRunrStorageProvider {

    @Override
    public StorageProvider get() {
        return new InMemoryStorageProvider();
    }
}
