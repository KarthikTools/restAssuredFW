package com.restassured.framework;

import com.restassured.framework.core.*;
import com.restassured.framework.model.TestCase;
import com.restassured.framework.model.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TestRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);
    
    public static void main(String[] args) {
        try {
            // Initialize components
            ExcelDataReader excelDataReader = new ExcelDataReader();
            VariableManager variableManager = new VariableManager();
            RequestBuilder requestBuilder = new RequestBuilder(variableManager);
            AssertionEngine assertionEngine = new AssertionEngine(variableManager);
            TestExecutor testExecutor = new TestExecutor(variableManager, requestBuilder, assertionEngine);
            
            // Read test suite
            File testFile = new File("src/test/resources/sample_test.csv");
            TestSuite testSuite = excelDataReader.readTestSuite(testFile);
            
            // Execute specific test case (TC_001 - Create User API Flow)
            String testCaseId = "TC_001";
            TestCase testCase = testSuite.getTestCases().get(testCaseId);
            
            if (testCase != null) {
                logger.info("Executing test case: {}", testCaseId);
                testExecutor.executeTestCase(testCase, testSuite);
                logger.info("Test case execution completed");
            } else {
                logger.error("Test case {} not found", testCaseId);
            }
            
        } catch (Exception e) {
            logger.error("Test execution failed", e);
        }
    }
} 