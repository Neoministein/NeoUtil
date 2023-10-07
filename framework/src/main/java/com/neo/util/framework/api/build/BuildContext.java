package com.neo.util.framework.api.build;

import com.neo.util.common.api.reflection.ReflectionProvider;

/**
 * The context for a {@link BuildStep}
 *
 * @param sourceOutPutDirectory the directory where source file should be generated in
 * @param resourceOutPutDirectory the directory where resource file should be generated in
 * @param targetDirectory the base directory where sources get compiled to
 * @param srcReflection ReflectionProvider with a classloader containing all classes which were compiled either in src or test
 * @param fullReflection ReflectionProvider with a classloader containing all classes including dependencies
 */
public record BuildContext(String sourceOutPutDirectory,
                           String resourceOutPutDirectory,
                           String targetDirectory,
                           ReflectionProvider srcReflection,
                           ReflectionProvider fullReflection) {
}
