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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Validates the method signature of {@link CronSchedule} and {@link FixedRateSchedule}
 */
public class SchedulerAnnotationSignatureBuildStep implements BuildStep {

    public static final ExceptionDetails EX_INVALID_METHOD_SIGNATURE = new ExceptionDetails(
            "compile/scheduler/invalid-method-signature",
            "The method [{0}.{1}] annotated with [{2}] is not allowed to have arguments."
    );

    public static final ExceptionDetails EX_DUPLICATED_SCHEDULER = new ExceptionDetails(
            "compile/scheduler/duplicated-scheduler-configured",
            "Duplicated scheduler id present [{0}] on method [{1}.{2}] and [{3}.{4}]");

    public static final ExceptionDetails INVALID_SCHEDULER_ID = new ExceptionDetails(
            "compile/scheduler/invalid-id",
            "The scheduler id [{0}] on method [{1}.{2}] may not have whitespaces.");

    private final Map<String, Method> schedulerNames = new HashMap<>();

    @Override
    public void execute(BuildContext context) {
        validateAnnotation(context, CronSchedule.class, CronSchedule::value);
        validateAnnotation(context, FixedRateSchedule.class, FixedRateSchedule::value);
    }

    protected <T extends Annotation> void validateAnnotation(BuildContext context, Class<T> annotation, Function<T, String> funcGetSchedulerId) {
        for (AnnotatedElement element: context.fullReflection().getAnnotatedElement(annotation)) {
            if (element instanceof Method method) {
                if (method.getParameters().length != 0) {
                    throw new ConfigurationException(EX_INVALID_METHOD_SIGNATURE, method.getDeclaringClass().getName(), method.getName(), annotation);
                }

                String schedulerName = funcGetSchedulerId.apply(element.getAnnotation(annotation));
                if (schedulerName.contains(" ")) {
                    throw new ConfigurationException(INVALID_SCHEDULER_ID, schedulerName, method.getDeclaringClass().getName(), method.getName());
                }

                Method duplicated = schedulerNames.put(schedulerName, method);
                if (duplicated != null) {
                    throw new ConfigurationException(EX_DUPLICATED_SCHEDULER, schedulerName,
                            method.getDeclaringClass().getName(), method.getName(),
                            duplicated.getDeclaringClass().getName(), duplicated.getName());
                }

            }
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
