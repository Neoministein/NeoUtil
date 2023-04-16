package com.neo.util.framework.api.build;

public interface BuildStep {

    /**
     * Execute a build step.
     *
     * @param context the context of the build operation (not {@code null})
     */
    void execute(BuildContext context);

    /**
     * The priory in which the build steps gets executed
     *
     * @return the priority
     */
    int priority();


    /**
     * The identifier should be unique for a build chain.
     *
     * @return the identifier
     */
    default String getId() {
        return this.getClass().getSimpleName();
    }
}