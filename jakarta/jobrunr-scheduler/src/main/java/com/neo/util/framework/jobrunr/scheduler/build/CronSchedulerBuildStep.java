package com.neo.util.framework.jobrunr.scheduler.build;

import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.scheduler.CronSchedule;
import com.neo.util.framework.api.scheduler.FixedRateSchedule;
import org.jobrunr.scheduling.cron.CronExpression;
import org.jobrunr.scheduling.cron.InvalidCronExpressionException;

/**
 * Validates the cron statement signature of {@link CronSchedule} and {@link FixedRateSchedule}
 */
public class CronSchedulerBuildStep implements BuildStep {

    public static final ExceptionDetails EX_INVALID_METHOD_SIGNATURE = new ExceptionDetails(
            "compile/scheduler/invalid-cron-statement",
            "The cron statement for scheduler [{0}] is invalid: [{1}]"
    );

    @Override
    public void execute(BuildContext context) {
        for (CronSchedule element: context.fullReflection().getAnnotationInstance(CronSchedule.class)) {
            try {
                CronExpression.create(element.cron());
            } catch (InvalidCronExpressionException ex) {
                throw new ConfigurationException(EX_INVALID_METHOD_SIGNATURE, element.value(), ex.getMessage());
            }
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
