package com.restassured.framework.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static String prettyPrint(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            logger.error("Error pretty printing JSON", e);
            return json;
        }
    }
    
    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON", e);
            return null;
        }
    }
    
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error converting object to JSON", e);
            return null;
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Error converting JSON to object", e);
            return null;
        }
    }
    
    public static String updateJson(String json, String path, String value) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String[] parts = path.split("\\.");
            JsonNode current = node;
            
            for (int i = 0; i < parts.length - 1; i++) {
                current = current.get(parts[i]);
                if (current == null) {
                    return json;
                }
            }
            
            if (current instanceof ObjectNode) {
                ((ObjectNode) current).put(parts[parts.length - 1], value);
            }
            
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            logger.error("Error updating JSON", e);
            return json;
        }
    }
    
    public static String extractValue(String json, String path) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String[] parts = path.split("\\.");
            JsonNode current = node;
            
            for (String part : parts) {
                current = current.get(part);
                if (current == null) {
                    return null;
                }
            }
            
            return current.asText();
        } catch (JsonProcessingException e) {
            logger.error("Error extracting value from JSON", e);
            return null;
        }
    }
    
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
} 