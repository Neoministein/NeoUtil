package com.neo.util.api.request;

import com.neo.util.api.request.user.RolePrincipal;
import com.neo.util.common.impl.exception.ValidationException;

import java.util.Optional;

public abstract class AbstractUserRequestDetails extends AbstractRequestDetails implements UserRequestDetails {

    protected RolePrincipal user = null;

    protected AbstractUserRequestDetails(String traceId, String instanceId, RequestContext requestContext) {
        super(traceId, instanceId, requestContext);
    }

    @Override
    public Optional<RolePrincipal> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public void setUserIfPossible(RolePrincipal user) {
        if (this.user == null) {
            this.user = user;
        } else {
            throw new ValidationException(EX_USER_ALREADY_DEFINED);
        }
    }
}
