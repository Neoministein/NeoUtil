package com.neo.util.framework.api.build;

/**
 * Build Steps are run after compile time and are used to generate and validate code for runtime use.
 * <p>
 * After all {@link BuildStep} have run, a second compiler will be executed on the files located in:
 *  - target/generated-sources/java
 *  - target/generated-test-sources/java
 * <p>
 * The implementation is instantiated using the default non arg constructor. Not having one will result in a compile
 * time failure.
 */
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