package com.restassured.framework.model;

import java.util.Map;
import java.util.List;

public class TestSuite {
    private Map<String, TestCase> testCases;
    private Map<String, TestStep> testSteps;
    private Map<String, List<Assertion>> assertions;
    private Map<String, List<Header>> headers;
    private Map<String, String> bodies;
    private Map<String, List<TestData>> testData;

    public Map<String, TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(Map<String, TestCase> testCases) {
        this.testCases = testCases;
    }

    public Map<String, TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(Map<String, TestStep> testSteps) {
        this.testSteps = testSteps;
    }

    public Map<String, List<Assertion>> getAssertions() {
        return assertions;
    }

    public void setAssertions(Map<String, List<Assertion>> assertions) {
        this.assertions = assertions;
    }

    public Map<String, List<Header>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<Header>> headers) {
        this.headers = headers;
    }

    public Map<String, String> getBodies() {
        return bodies;
    }

    public void setBodies(Map<String, String> bodies) {
        this.bodies = bodies;
    }

    public Map<String, List<TestData>> getTestData() {
        return testData;
    }

    public void setTestData(Map<String, List<TestData>> testData) {
        this.testData = testData;
    }
} 