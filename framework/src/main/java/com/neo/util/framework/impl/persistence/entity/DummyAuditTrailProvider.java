package com.neo.util.framework.impl.persistence.entity;

import com.neo.util.framework.api.request.RequestDetails;
import com.neo.util.framework.api.persistence.entity.AuditParameter;
import com.neo.util.framework.api.persistence.entity.AuditTrailProvider;
import com.neo.util.framework.api.persistence.entity.PersistenceEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DummyAuditTrailProvider implements AuditTrailProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DummyAuditTrailProvider.class);

    @Inject
    protected Provider<RequestDetails> requestDetailsProvider;

    @Override
    public void audit(PersistenceEntity entity, AuditParameter auditParameter) {
        LOGGER.info("AuditOperation by [{}] for [{}] primary key [{}] Operation: [{}]",
                requestDetailsProvider.get().getInitiator(),
                entity.getClass().getSimpleName(),
                entity.getPrimaryKey(),
                auditParameter.getOperation());
    }
}
