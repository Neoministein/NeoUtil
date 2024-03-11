package com.neo.util.framework.build;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Validates the method signature of {@link CronSchedule} and {@link FixedRateSchedule}
 */
public class SchedulerAnnoationSignatureBuildStep implements BuildStep {

    public static final ExceptionDetails EX_INVALID_METHOD_SIGNATURE = new ExceptionDetails(
            "compile/scheduler/invalid-method-signature",
            "The method [{0}.{1}] annotated with [{2}] is not allowed to have arguments."
    );

    @Override
    public void execute(BuildContext context) {
        validateAnnotation(context, CronSchedule.class);
        validateAnnotation(context, FixedRateSchedule.class);
    }

    protected void validateAnnotation(BuildContext context, Class<? extends Annotation> annotation) {
        for (AnnotatedElement element: context.fullReflection().getAnnotatedElement(annotation)) {
            if (element instanceof Method method && method.getParameters().length != 0) {
                throw new ConfigurationException(EX_INVALID_METHOD_SIGNATURE, method.getDeclaringClass().getName(), method.getName(), annotation);
            }
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
