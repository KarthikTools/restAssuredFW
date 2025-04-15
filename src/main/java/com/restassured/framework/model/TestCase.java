package com.restassured.framework.model;

import java.util.List;

public class TestCase {
    private String testCaseId;
    private String testCaseName;
    private String description;
    private boolean execute;
    private String preRequisite;
    private List<String> testSteps;

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExecute() {
        return execute;
    }

    public void setExecute(boolean execute) {
        this.execute = execute;
    }

    public String getPreRequisite() {
        return preRequisite;
    }

    public void setPreRequisite(String preRequisite) {
        this.preRequisite = preRequisite;
    }

    public List<String> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<String> testSteps) {
        this.testSteps = testSteps;
    }
} 