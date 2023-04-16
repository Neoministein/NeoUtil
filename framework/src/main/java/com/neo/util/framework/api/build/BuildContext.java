package com.neo.util.framework.api.build;

public class BuildContext {

    protected final String sourceOutPutDirectory;
    protected final String resourceOutPutDirectory;

    protected final ClassLoader srcLoader;
    protected final ClassLoader fullLoader;

    public BuildContext(String sourceOutPutDirectory, String resourceOutPutDirectory, ClassLoader srcLoader, ClassLoader fullLoader) {
        this.sourceOutPutDirectory = sourceOutPutDirectory;
        this.resourceOutPutDirectory = resourceOutPutDirectory;
        this.srcLoader = srcLoader;
        this.fullLoader = fullLoader;
    }

    public String getSourceOutPutDirectory() {
        return sourceOutPutDirectory;
    }

    public String getResourceOutPutDirectory() {
        return resourceOutPutDirectory;
    }

    public ClassLoader getSrcLoader() {
        return srcLoader;
    }

    public ClassLoader getFullLoader() {
        return fullLoader;
    }
}
