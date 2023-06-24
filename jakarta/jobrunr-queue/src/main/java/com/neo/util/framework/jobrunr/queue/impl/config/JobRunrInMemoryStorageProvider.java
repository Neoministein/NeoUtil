package com.neo.util.framework.jobrunr.queue.impl.config;

import com.neo.util.framework.jobrunr.queue.api.JobRunrStorageProvider;
import jakarta.enterprise.context.Dependent;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;

@Dependent
public class JobRunrInMemoryStorageProvider implements JobRunrStorageProvider {

    @Override
    public StorageProvider get() {
        return new InMemoryStorageProvider();
    }
}
