package com.neo.util.framework.build;

import com.neo.util.common.impl.annotation.ReflectionUtils;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import com.neo.util.framework.api.persistence.search.Searchable;
import com.neo.util.framework.api.persistence.search.SearchableIndex;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

public class SearchableAnnotationBuildStep implements BuildStep {

    public static final String SUPPRESS_TYPE = "neoutil:SearchableIndex";

    public static final ExceptionDetails EX_MISSING_ANNOTATION = new ExceptionDetails(
            "compile/searchable/missing-annotation",
            "The class [{}] which is implementing Searchable is missing the annotation [{}]", true
    );

    @Override
    public void execute(BuildContext buildContext) {
        Set<Class<? extends Searchable>> searchableClasses = ReflectionUtils.getSubTypesOf(Searchable.class, buildContext.fullLoader());
        for (Class<? extends Searchable> clazz: searchableClasses) {
            if (Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            SearchableIndex searchableIndex = clazz.getAnnotation(SearchableIndex.class);
            if (searchableIndex != null) {
                continue;
            }

            SuppressWarnings suppressWarnings = clazz.getAnnotation(SuppressWarnings.class);
            if (suppressWarnings != null &&
                    Arrays.asList(suppressWarnings.value()).contains(SUPPRESS_TYPE)) {
                continue;
            }
            throw new ValidationException(EX_MISSING_ANNOTATION, clazz.getName(), SearchableIndex.class.getName());
        }
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
}
