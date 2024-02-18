package com.neo.util.framework.impl.request;

import com.neo.util.framework.api.request.RequestAuditProvider;
import com.neo.util.framework.api.request.RequestDetails;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Slf4jRequestAuditProvider implements RequestAuditProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jRequestAuditProvider.class);

    @Override
    public <T extends RequestDetails> void audit(T requestDetails, boolean failed) {
        LOGGER.info("Request AuditOperation status: [{}], Type [{}]: {}",failed, requestDetails.getClass().getSimpleName(), requestDetails);
    }
}