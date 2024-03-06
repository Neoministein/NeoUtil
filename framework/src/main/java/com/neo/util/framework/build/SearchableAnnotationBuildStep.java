package com.neo.util.framework.build;

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

/**
 * Validates that all classes implementing {@link Searchable} are annotated with {@link SearchableIndex}
 * otherwise, a {@link ValidationException} is thrown.
 * <p>
 * It won't be thrown when:
 * <ul>
 * <li>The class is Abstract</li>
 * <li>The class is annotated with {@link SuppressWarnings} containing {@link SearchableAnnotationBuildStep#SUPPRESS_TYPE}</li>
 * </ul>
 */
public class SearchableAnnotationBuildStep implements BuildStep {

    public static final String SUPPRESS_TYPE = "neoutil:SearchableIndex";

    public static final ExceptionDetails EX_MISSING_ANNOTATION = new ExceptionDetails(
            "compile/searchable/missing-annotation",
            "The class [{0}] which is implementing Searchable is missing the annotation [{1}]"
    );

    @Override
    public void execute(BuildContext buildContext) {
        Set<Class<? extends Searchable>> searchableClasses = buildContext.fullReflection().getSubTypesOf(Searchable.class);
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
