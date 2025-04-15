package com.restassured.framework.core;

import com.restassured.framework.model.TestCase;
import com.restassured.framework.model.TestStep;
import com.restassured.framework.model.TestSuite;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);
    private final VariableManager variableManager;
    private final RequestBuilder requestBuilder;
    private final AssertionEngine assertionEngine;
    
    public TestExecutor(VariableManager variableManager, RequestBuilder requestBuilder, AssertionEngine assertionEngine) {
        this.variableManager = variableManager;
        this.requestBuilder = requestBuilder;
        this.assertionEngine = assertionEngine;
    }
    
    public void executeTestSuite(TestSuite testSuite) {
        logger.info("Starting test suite execution");
        
        for (Map.Entry<String, TestCase> entry : testSuite.getTestCases().entrySet()) {
            TestCase testCase = entry.getValue();
            
            if (!testCase.isExecute()) {
                logger.info("Skipping test case {} as it is marked for non-execution", testCase.getTestCaseId());
                continue;
            }
            
            executeTestCase(testCase, testSuite);
        }
    }
    
    public void executeTestCase(TestCase testCase, TestSuite testSuite) {
        logger.info("Executing test case: {}", testCase.getTestCaseId());
        
        // Initialize test case variables
        variableManager.initializeTestCaseVariables(
            testCase.getTestCaseId(), 
            testSuite.getTestData().get(testCase.getTestCaseId())
        );
        
        // Execute prerequisite test case if specified
        if (testCase.getPreRequisite() != null && !testCase.getPreRequisite().isEmpty()) {
            TestCase prerequisite = testSuite.getTestCases().get(testCase.getPreRequisite());
            if (prerequisite != null) {
                executeTestCase(prerequisite, testSuite);
            }
        }
        
        // Execute test steps
        for (String stepId : testCase.getTestSteps()) {
            TestStep testStep = testSuite.getTestSteps().get(stepId);
            if (testStep != null) {
                executeTestStep(testStep, testSuite);
            }
        }
        
        // Clear test case variables
        variableManager.clearTestCaseVariables();
    }
    
    private void executeTestStep(TestStep testStep, TestSuite testSuite) {
        logger.info("Executing test step: {}", testStep.getStepId());
        
        try {
            // Execute request
            Response response = requestBuilder.executeRequest(
                testStep,
                testSuite.getHeaders(),
                testSuite.getBodies()
            );
            
            // Validate response status code
            if (response.getStatusCode() != testStep.getExpectedStatus()) {
                logger.error("Status code mismatch. Expected: {}, Actual: {}", 
                    testStep.getExpectedStatus(), 
                    response.getStatusCode());
                throw new AssertionError("Status code mismatch");
            }
            
            // Validate assertions if specified
            if (testStep.getAssertions() != null && !testStep.getAssertions().isEmpty()) {
                boolean assertionsPassed = assertionEngine.validateAssertions(
                    response,
                    testStep.getAssertions(),
                    testSuite.getAssertions()
                );
                
                if (!assertionsPassed) {
                    throw new AssertionError("Assertions failed");
                }
            }
            
            // Save response if required
            if (testStep.isSaveResponse()) {
                saveResponse(response, testStep.getStepId());
            }
            
        } catch (Exception e) {
            logger.error("Test step {} failed", testStep.getStepId(), e);
            throw e;
        } finally {
            // Clear step variables
            variableManager.clearStepVariables();
        }
    }
    
    private void saveResponse(Response response, String stepId) {
        // Implementation for saving response would go here
        // This could save to a file, database, or other storage
        logger.info("Response for step {}: {}", stepId, response.asString());
    }
} 