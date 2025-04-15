package com.restassured.framework.core;

import com.restassured.framework.model.TestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableManager {
    private static final Logger logger = LoggerFactory.getLogger(VariableManager.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\{\\{\\$(.*?)\\((.*?)\\)\\}\\}");
    
    private final Map<String, String> globalVariables;
    private final Map<String, String> testCaseVariables;
    private final Map<String, String> stepVariables;
    
    public VariableManager() {
        this.globalVariables = new HashMap<>();
        this.testCaseVariables = new HashMap<>();
        this.stepVariables = new HashMap<>();
    }
    
    public void initializeTestCaseVariables(String testCaseId, List<TestData> testData) {
        testCaseVariables.clear();
        stepVariables.clear();
        
        if (testData != null) {
            for (TestData data : testData) {
                if (testCaseId.equals(data.getTestCaseId())) {
                    testCaseVariables.put(data.getVarName(), data.getValue());
                }
            }
        }
    }
    
    public void setStepVariable(String name, String value) {
        stepVariables.put(name, value);
    }
    
    public String substituteVariables(String input) {
        if (input == null) {
            return null;
        }
        
        String result = input;
        Matcher matcher = VARIABLE_PATTERN.matcher(result);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = getVariableValue(variableName);
            
            if (value != null) {
                result = result.replace("{{" + variableName + "}}", value);
            }
        }
        
        return result;
    }
    
    private String getVariableValue(String variableName) {
        // Check for function calls
        Matcher functionMatcher = FUNCTION_PATTERN.matcher(variableName);
        if (functionMatcher.matches()) {
            String functionName = functionMatcher.group(1);
            String parameters = functionMatcher.group(2);
            return executeFunction(functionName, parameters);
        }
        
        // Check variables in order of precedence
        String value = stepVariables.get(variableName);
        if (value != null) return value;
        
        value = testCaseVariables.get(variableName);
        if (value != null) return value;
        
        value = globalVariables.get(variableName);
        if (value != null) return value;
        
        logger.warn("Variable {} not found in any scope", variableName);
        return null;
    }
    
    private String executeFunction(String functionName, String parameters) {
        switch (functionName.toLowerCase()) {
            case "randomuuid":
                return UUID.randomUUID().toString();
            case "timestamp":
                return String.valueOf(System.currentTimeMillis());
            case "randomstring":
                int length = parameters.isEmpty() ? 10 : Integer.parseInt(parameters);
                return generateRandomString(length);
            default:
                logger.warn("Unknown function: {}", functionName);
                return null;
        }
    }
    
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
    
    public void clearStepVariables() {
        stepVariables.clear();
    }
    
    public void clearTestCaseVariables() {
        testCaseVariables.clear();
        stepVariables.clear();
    }
    
    public void clearAllVariables() {
        globalVariables.clear();
        testCaseVariables.clear();
        stepVariables.clear();
    }
} 