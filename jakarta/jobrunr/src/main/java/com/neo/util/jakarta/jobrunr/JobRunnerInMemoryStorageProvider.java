package com.neo.util.jakarta.jobrunr;

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
