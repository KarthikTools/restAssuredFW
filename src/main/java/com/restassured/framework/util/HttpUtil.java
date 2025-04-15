package com.restassured.framework.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    
    public static Response get(String url, Map<String, String> headers) {
        return executeRequest("GET", url, headers, null);
    }
    
    public static Response post(String url, Map<String, String> headers, String body) {
        return executeRequest("POST", url, headers, body);
    }
    
    public static Response put(String url, Map<String, String> headers, String body) {
        return executeRequest("PUT", url, headers, body);
    }
    
    public static Response delete(String url, Map<String, String> headers) {
        return executeRequest("DELETE", url, headers, null);
    }
    
    public static Response patch(String url, Map<String, String> headers, String body) {
        return executeRequest("PATCH", url, headers, body);
    }
    
    private static Response executeRequest(String method, String url, Map<String, String> headers, String body) {
        logger.debug("Executing {} request to {}", method, url);
        
        RequestSpecification request = RestAssured.given()
            .baseUri(ConfigUtil.getProperty("api.base.url"))
            .contentType(ContentType.JSON);
        
        // Add headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.header(entry.getKey(), entry.getValue());
            }
        }
        
        // Add body if present
        if (body != null) {
            request.body(body);
        }
        
        // Execute request
        Response response = request.request(method, url);
        
        // Log response
        logger.debug("Response status: {}", response.getStatusCode());
        logger.debug("Response body: {}", response.getBody().asString());
        
        return response;
    }
    
    public static boolean isSuccessResponse(Response response) {
        int statusCode = response.getStatusCode();
        return statusCode >= 200 && statusCode < 300;
    }
    
    public static String getResponseBody(Response response) {
        return response.getBody().asString();
    }
    
    public static int getStatusCode(Response response) {
        return response.getStatusCode();
    }
    
    public static Map<String, String> getHeaders(Response response) {
        return response.getHeaders().asList().stream()
            .collect(java.util.stream.Collectors.toMap(
                header -> header.getName(),
                header -> header.getValue()
            ));
    }
    
    public static String getHeader(Response response, String headerName) {
        return response.getHeader(headerName);
    }
    
    public static String getCookie(Response response, String cookieName) {
        return response.getCookie(cookieName);
    }
    
    public static Map<String, String> getCookies(Response response) {
        return response.getCookies();
    }
    
    public static String extractValue(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }
    
    public static <T> T extractValue(Response response, String jsonPath, Class<T> type) {
        return response.jsonPath().getObject(jsonPath, type);
    }
} 