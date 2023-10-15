package com.neo.util.framework.impl.build;

import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BuildStepExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildStepExecutor.class);

    public void executeBuildSteps(BuildContext buildContext) {
        Set<Class<? extends BuildStep>> buildStepClasses = buildContext.fullReflection().getSubTypesOf(BuildStep.class);

        List<BuildStep> buildSteps = buildStepClasses.stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .map(this::instantiate)
                .sorted(Comparator.comparingInt(BuildStep::priority))
                .toList();

        for (BuildStep buildStep: buildSteps) {
            LOGGER.info("Executing BuildStep: {}", buildStep.getId());
            buildStep.execute(buildContext);
        }
    }

    protected BuildStep instantiate(Class<? extends BuildStep> buildStep) {
        try {
            LOGGER.trace("Instantiating BuildStep constructor of class [{}]", buildStep.getName());
            return buildStep.getConstructor().newInstance();
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("No default constructor found on BuildStep [="
                    + buildStep.getName() + "]", ex);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("BuildStep instantiation failed", ex);
        }
    }
}
