package com.restassured.framework.core;

import com.restassured.framework.model.Header;
import com.restassured.framework.model.TestStep;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RequestBuilder.class);
    private final VariableManager variableManager;
    
    public RequestBuilder(VariableManager variableManager) {
        this.variableManager = variableManager;
    }
    
    public Response executeRequest(TestStep testStep, Map<String, List<Header>> headersMap, Map<String, String> bodiesMap) {
        logger.info("Executing request for step: {}", testStep.getStepId());
        
        // Get base URL from configuration
        String baseUrl = System.getProperty("api.base.url", "http://localhost:8080");
        
        // Build request specification
        RequestSpecification requestSpec = RestAssured.given()
            .baseUri(baseUrl)
            .contentType(ContentType.JSON);
        
        // Apply headers
        applyHeaders(requestSpec, testStep.getHeaders(), headersMap);
        
        // Apply body if present
        if (testStep.getBody() != null && !testStep.getBody().isEmpty()) {
            String bodyTemplate = bodiesMap.get(testStep.getBody());
            if (bodyTemplate != null) {
                String substitutedBody = variableManager.substituteVariables(bodyTemplate);
                requestSpec.body(substitutedBody);
            }
        }
        
        // Execute request
        String endpoint = variableManager.substituteVariables(testStep.getEndpoint());
        Method method = Method.valueOf(testStep.getMethod().toUpperCase());
        
        logger.debug("Sending {} request to {}", method, endpoint);
        Response response = requestSpec.request(method, endpoint);
        
        // Extract variables if specified
        extractVariables(response, testStep.getExtractVars());
        
        return response;
    }
    
    private void applyHeaders(RequestSpecification requestSpec, String headerId, Map<String, List<Header>> headersMap) {
        if (headerId != null && !headerId.isEmpty()) {
            List<Header> headers = headersMap.get(headerId);
            if (headers != null) {
                for (Header header : headers) {
                    String value = variableManager.substituteVariables(header.getValue());
                    requestSpec.header(header.getKey(), value);
                }
            }
        }
    }
    
    private void extractVariables(Response response, String extractVars) {
        if (extractVars != null && !extractVars.isEmpty()) {
            String[] varDefinitions = extractVars.split(",");
            for (String varDef : varDefinitions) {
                String[] parts = varDef.trim().split("=");
                if (parts.length == 2) {
                    String varName = parts[0].trim();
                    String jsonPath = parts[1].trim();
                    String value = response.jsonPath().getString(jsonPath);
                    variableManager.setStepVariable(varName, value);
                    logger.debug("Extracted variable {} = {}", varName, value);
                }
            }
        }
    }
} 