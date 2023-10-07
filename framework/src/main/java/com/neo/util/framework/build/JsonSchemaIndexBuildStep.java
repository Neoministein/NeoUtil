package com.neo.util.framework.build;

import com.neo.util.framework.api.FrameworkConstants;
import com.neo.util.framework.api.PriorityConstants;
import com.neo.util.framework.api.build.BuildContext;
import com.neo.util.framework.api.build.BuildStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

/**
 * Generates an index file that contains the locations of all json schemas. Located at {@link FrameworkConstants#JSON_SCHEMA_LOCATION}
 * <p>
 * The index file will be generated at: {@link FrameworkConstants#JSON_SCHEMA_INDEX}
 */
public class JsonSchemaIndexBuildStep implements BuildStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaIndexBuildStep.class);

    @Override
    public void execute(BuildContext context) {
        LOGGER.info("Generating index files for all json schemas located inside {}", FrameworkConstants.JSON_SCHEMA_LOCATION);

        Set<String> srcFiles = context.fullReflection().getResources("^configuration/schema.*\\.json$");

        generateIndexFile(srcFiles, context.resourceOutPutDirectory());
    }

    @Override
    public int priority() {
        return PriorityConstants.PLATFORM_AFTER;
    }
    protected void generateIndexFile(Set<String> files, String outputDir) {
        String filePath = outputDir.concat("/").concat(FrameworkConstants.JSON_SCHEMA_INDEX);
        try {
            File indexFile = new File(filePath);
            indexFile.getParentFile().mkdirs();
            Files.writeString(indexFile.toPath(), String.join("\n", files));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.info("Generated index file at {}", filePath);
    }
}
