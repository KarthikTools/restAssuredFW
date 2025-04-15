package com.restassured.framework;

import com.restassured.framework.core.*;
import com.restassured.framework.model.TestSuite;
import org.testng.annotations.Test;

import java.io.File;

public class TestNGRunner {
    private final ExcelDataReader excelDataReader;
    private final VariableManager variableManager;
    private final RequestBuilder requestBuilder;
    private final AssertionEngine assertionEngine;
    private final TestExecutor testExecutor;
    
    public TestNGRunner() {
        this.excelDataReader = new ExcelDataReader();
        this.variableManager = new VariableManager();
        this.requestBuilder = new RequestBuilder(variableManager);
        this.assertionEngine = new AssertionEngine(variableManager);
        this.testExecutor = new TestExecutor(variableManager, requestBuilder, assertionEngine);
    }
    
    @Test
    public void executeTestSuite() {
        try {
            // Get test file path from system property or use default
            String testFile = System.getProperty("test.file", "test_suite.xlsx");
            File file = new File(testFile);
            
            if (!file.exists()) {
                throw new RuntimeException("Test file not found: " + testFile);
            }
            
            // Read test suite from Excel/CSV
            TestSuite testSuite = excelDataReader.readTestSuite(file);
            
            // Execute test suite
            testExecutor.executeTestSuite(testSuite);
            
        } catch (Exception e) {
            throw new RuntimeException("Test execution failed", e);
        }
    }
    
    public static void main(String[] args) {
        TestNGRunner runner = new TestNGRunner();
        runner.executeTestSuite();
    }
} 