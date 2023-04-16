package com.neo.util.framework.build;

import com.neo.util.common.impl.annotation.ReflectionUtils;
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

public class JsonSchemaIndexBuildStep implements BuildStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaIndexBuildStep.class);

    @Override
    public void execute(BuildContext context) {
        LOGGER.info("Generating index files for all json schemas located inside {}", FrameworkConstants.JSON_SCHEMA_LOCATION);

        Set<String> srcFiles = ReflectionUtils.getResources(
                FrameworkConstants.JSON_SCHEMA_LOCATION, ReflectionUtils.JSON_FILE_ENDING, context.getSrcLoader());
        generateIndexFile(srcFiles, context.getResourceOutPutDirectory());
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
