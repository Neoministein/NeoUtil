package com.neo.util.framework.api.build;

/**
 * The context for a {@link BuildStep}
 *
 * @param sourceOutPutDirectory the directory where source file should be generated in
 * @param resourceOutPutDirectory the directory where resource file should be generated in
 * @param srcLoader a classloader containing all classes which were compiled either in src or test
 * @param fullLoader a classloader containing all classes including dependencies
 */
public record BuildContext(String sourceOutPutDirectory,
                           String resourceOutPutDirectory,
                           ClassLoader srcLoader,
                           ClassLoader fullLoader) {
}
