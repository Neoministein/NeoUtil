package com.neo.util.framework.mapping.impl;

import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.mapping.api.MappingSchema;
import com.neo.util.framework.mapping.api.MappingSchemaHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class MappingSchemaHolderImpl implements MappingSchemaHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingSchemaHolderImpl.class);

    protected final ConfigService configService;
    protected final MappingSchemaFactory factory;


    protected Map<String, MappingSchema> schemaMap = new HashMap<>();

    @Inject
    public MappingSchemaHolderImpl(ConfigService configService, MappingSchemaFactory factory) {
        this.configService = configService;
        this.factory = factory;
        reload();
    }

    @Override
    public void reload() throws IllegalArgumentException {
        LOGGER.info("Loading MappingSchemas");
        Map<String, MappingSchema> newSchemaMap = new HashMap<>();

        File mappingLocation = new File(configService.get("mapping-schema.location").asString().get() + "/configuration/cfg/workflow");
        LOGGER.info("Mapping File Location: [{}]", mappingLocation.getAbsolutePath());
        for (File location: mappingLocation.listFiles()) {
            LOGGER.info("Loading Mapping [{}]", location.getName());
            try {
                String jsonSchema = Files.readString(Path.of(location.getPath() + "\\json-schema.json"));
                String mappingSchema = Files.readString(Path.of(location.getPath() + "\\mapping-schema.xml"));
                newSchemaMap.put(location.getName(), factory.createSchema(jsonSchema, mappingSchema));

            } catch (IOException ex) {
                throw new IllegalArgumentException("", ex);
            }
        }

        this.schemaMap = newSchemaMap;
    }

    @Override
    public Set<String> getMappingSchemaNames() {
        return schemaMap.keySet();
    }

    @Override
    public MappingSchema fetchMappingSchema(String schema) {
        return schemaMap.get(schema);
    }
}
