package com.restassured.framework.core;

import com.restassured.framework.model.Assertion;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class AssertionEngine {
    private static final Logger logger = LoggerFactory.getLogger(AssertionEngine.class);
    private final VariableManager variableManager;
    
    public AssertionEngine(VariableManager variableManager) {
        this.variableManager = variableManager;
    }
    
    public boolean validateAssertions(Response response, String assertionId, Map<String, List<Assertion>> assertionsMap) {
        List<Assertion> assertions = assertionsMap.get(assertionId);
        if (assertions == null || assertions.isEmpty()) {
            logger.warn("No assertions found for ID: {}", assertionId);
            return true;
        }
        
        boolean allPassed = true;
        for (Assertion assertion : assertions) {
            boolean passed = validateAssertion(response, assertion);
            if (!passed) {
                allPassed = false;
                logger.error("Assertion failed: {} - Expected: {}, Actual: {}", 
                    assertion.getValidationType(), 
                    assertion.getExpected(), 
                    assertion.getActual());
            }
        }
        
        return allPassed;
    }
    
    private boolean validateAssertion(Response response, Assertion assertion) {
        switch (assertion.getValidationType().toLowerCase()) {
            case "database":
                return validateDatabaseAssertion(assertion);
            case "responsebody":
                return validateResponseBodyAssertion(response, assertion);
            case "responsemetatype":
                return validateResponseMetaTypeAssertion(response, assertion);
            case "kibana":
                return validateKibanaAssertion(assertion);
            default:
                logger.warn("Unknown assertion type: {}", assertion.getValidationType());
                return false;
        }
    }
    
    private boolean validateDatabaseAssertion(Assertion assertion) {
        try {
            String dbUrl = System.getProperty("db.url");
            String dbUser = System.getProperty("db.user");
            String dbPassword = System.getProperty("db.password");
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(assertion.getValidationInputType())) {
                
                if (rs.next()) {
                    String actualValue = rs.getString(1);
                    assertion.setActual(actualValue);
                    return actualValue.equals(assertion.getExpected());
                }
            }
        } catch (Exception e) {
            logger.error("Database assertion failed", e);
        }
        return false;
    }
    
    private boolean validateResponseBodyAssertion(Response response, Assertion assertion) {
        try {
            String actualValue = response.jsonPath().getString(assertion.getValidationInputType());
            assertion.setActual(actualValue);
            return actualValue.equals(assertion.getExpected());
        } catch (Exception e) {
            logger.error("Response body assertion failed", e);
            return false;
        }
    }
    
    private boolean validateResponseMetaTypeAssertion(Response response, Assertion assertion) {
        try {
            String actualValue = response.jsonPath().getString(assertion.getValidationInputType());
            assertion.setActual(actualValue);
            return actualValue.equals(assertion.getExpected());
        } catch (Exception e) {
            logger.error("Response meta type assertion failed", e);
            return false;
        }
    }
    
    private boolean validateKibanaAssertion(Assertion assertion) {
        // Implementation for Kibana log validation would go here
        // This would typically involve querying Kibana's API
        logger.warn("Kibana assertion validation not implemented");
        return false;
    }
} 