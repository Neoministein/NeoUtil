package com.neo.util.framework.impl.excpetion;

import com.neo.util.common.impl.exception.ExternalRuntimeException;
import com.neo.util.common.impl.exception.InternalRuntimeException;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.excpetion.ToExternalException;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Set;

@ToExternalException(value = {}) // The `value` attribute is @Nonbinding.
@Interceptor
@Priority(PriorityConstants.LIBRARY_AFTER)
public class ToExternalExceptionInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Throwable {
        try {
            return invocationContext.proceed();
        } catch (InternalRuntimeException ex) {
            if (getAllowedExceptionIds(invocationContext).contains(ex.getExceptionId())) {
                throw new ExternalRuntimeException(ex);
            } else {
                throw ex;
            }
        }
    }

    public Set<String> getAllowedExceptionIds(InvocationContext invocationContext) {
        ToExternalException methodAnnotation = invocationContext.getMethod().getAnnotation(ToExternalException.class);

        if (methodAnnotation != null) {
            return Set.of(methodAnnotation.value());
        }

        ToExternalException classAnnotation = invocationContext.getMethod().getAnnotation(ToExternalException.class);
        if (classAnnotation != null) {
            return Set.of(classAnnotation.value());
        }

        return Set.of();
    }
}
