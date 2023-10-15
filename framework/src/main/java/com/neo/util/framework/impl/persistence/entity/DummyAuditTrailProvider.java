package com.neo.util.framework.impl.persistence.entity;

import com.neo.util.framework.api.persistence.entity.AuditParameter;
import com.neo.util.framework.api.persistence.entity.EntityAuditTrailProvider;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import com.neo.util.framework.api.request.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DummyAuditTrailProvider implements EntityAuditTrailProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyAuditTrailProvider.class);

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Override
    public void audit(PersistenceEntity entity, AuditParameter auditParameter) {
        LOGGER.info("Entity AuditOperation by [{}] for [{}] primary key [{}] Operation: [{}]",
                requestDetailsProvider.get().getInitiator(),
                entity.getClass().getSimpleName(),
                entity.getPrimaryKey(),
                auditParameter.getOperation());
    }
}
