package com.neo.util.common.impl.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neo.util.common.impl.exception.InternalJsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("java:S2139")
public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    /**
     * https://stackoverflow.com/questions/3907929/should-i-declare-jacksons-objectmapper-as-a-static-field
     *
     * Yes ObjectMapper is safe and static final is recommended.
     *
     * The only caveat is that you can't be modifying configuration of the mapper once it is shared;
     * but you are not changing configuration so that is fine. If you did need to change configuration,
     * you would do that from the static block, and it would be fine as well.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {}

    static {
        // ignore null fields
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // use fields only
        MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        MAPPER.setTimeZone(TimeZone.getDefault());

        // Don't throw error when empty bean is being serialize
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Create a Json String from a pojo. Exceptions are caught and a new {@link InternalJsonException} is
     * thrown.
     *
     * @param pojo to object convert
     *
     * @return the json string
     */
    public static String toJson(Object pojo) {
        String jsonString;

        try {
            jsonString = MAPPER.writeValueAsString(pojo);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Error while creating json string from pojo:{}, exception:{} ", pojo, ex.getMessage());
            throw new InternalJsonException(ex.getMessage(), ex);
        }
        return jsonString;
    }

    public static JsonNode fromJson(String json) {
        try {
            return MAPPER.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Error while parsing JSON node from json string:{}, exception:{} ", json, ex.getMessage());
            throw new InternalJsonException(ex.getMessage(), ex);
        }
    }

    /**
     * Create a Json String from a pojo. Exceptions are caught and a new {@link InternalJsonException} is
     * thrown.
     *
     * @param pojo to object convert
     * @param serializationScope the jackson serialization scope
     *
     * @return the json string
     */
    public static String toJson(Object pojo, Class<?> serializationScope) {
        try {
            return MAPPER.writerWithView(serializationScope).writeValueAsString(pojo);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Error while creating json string from pojo:{}, exception:{} ", pojo, ex.getMessage());
            throw new InternalJsonException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates an object based on the provided class and json. Exceptions are caught and a new {@link InternalJsonException} is
     * thrown.
     *
     * @param json the json string
     * @param clazz the class of to object to convert to
     * @param <T> the object to convert to
     *
     * @return the object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException ex) {
            LOGGER.error("Error while creating pojo from json string:{}, exception:{} ", json, ex.getMessage());

            throw new InternalJsonException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates an object based on the provided class and json. Exceptions are caught and a new {@link InternalJsonException} is
     * thrown.
     *
     * @param json the json string
     * @param clazz the class of to object to convert to
     * @param serializationScope the jackson serialization scope
     * @param <T>  the object to convert to
     *
     * @return the object
     */
    public static <T> T fromJson(String json, Class<T> clazz, Class<?> serializationScope) {
        try {
            return MAPPER.readerWithView(serializationScope).readValue(json, clazz);
        } catch (IOException ex) {
            LOGGER.error("Error while creating pojo from json string:{}, exception:{} ", json, ex.getMessage());
            throw new InternalJsonException(ex.getMessage(), ex);
        }
    }

    /**
     * Creates an empty {@link ObjectNode}
     */
    public static ObjectNode emptyObjectNode() {
        return MAPPER.createObjectNode();
    }

    /**
     * Creates an empty {@link ArrayNode}
     */
    public static ArrayNode emptyArrayNode() {
        return MAPPER.createArrayNode();
    }

    /**
     * Converts an object to to a {@link ObjectNode}.
     *
     * @param pojo to object convert
     *
     * @return the ObjectNode
     */
    public static <T extends JsonNode> T fromPojo(Object pojo) {
        if (pojo == null) {
            return null;
        }
        return MAPPER.valueToTree(pojo);
    }

    /**
     * Adds the json to the existing entity based on the serialization scope. Exceptions are caught and a new {@link InternalJsonException} is
     * thrown.
     *
     * @param pojo the object to add to
     * @param json the json to add to the object
     * @param clazz the class of the object
     * @param serializationScope the jack son serialization scope
     * @param <T> the object type
     * @return
     */
    public static <T> T updateExistingEntity(T pojo, String json, Class<T> clazz, Class<?> serializationScope) {
        try {
            return MAPPER.readerForUpdating(pojo).withView(serializationScope).readValue(json, clazz);
        } catch (IOException ex) {
            LOGGER.error("Error while updating existing pojo from json string:{}, exception:{} ", json, ex.getMessage());
            throw new InternalJsonException(ex.getMessage(), ex);
        }
    }

    /**
     * Merges the two {@link JsonNode} together
     *
     * @param mainNode merge into Node
     * @param updateNode merge from Node
     * @return the merged Node
     */
    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();

        while (fieldNames.hasNext()) {
            String updatedFieldName = fieldNames.next();
            JsonNode valueToBeUpdated = mainNode.get(updatedFieldName);
            JsonNode updatedValue = updateNode.get(updatedFieldName);

            if (valueToBeUpdated != null && valueToBeUpdated.isArray() && updatedValue.isArray()) {
                for (int i = 0; i < updatedValue.size(); i++) {
                    JsonNode updatedChildNode = updatedValue.get(i);
                    if (valueToBeUpdated.size() <= i) {
                        ((ArrayNode) valueToBeUpdated).add(updatedChildNode);
                    }
                    JsonNode childNodeToBeUpdated = valueToBeUpdated.get(i);
                    merge(childNodeToBeUpdated, updatedChildNode);
                }
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject()) {
                merge(valueToBeUpdated, updatedValue);
            } else {
                if (mainNode instanceof ObjectNode objectNode) {
                    objectNode.replace(updatedFieldName, updatedValue);
                }
            }
        }
        return mainNode;
    }
}
