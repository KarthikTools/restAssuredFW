package com.restassured.framework.model;

public class TestStep {
    private String stepId;
    private String apiName;
    private String method;
    private String endpoint;
    private String headers;
    private String body;
    private String extractVars;
    private int expectedStatus;
    private String assertions;
    private boolean saveResponse;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getExtractVars() {
        return extractVars;
    }

    public void setExtractVars(String extractVars) {
        this.extractVars = extractVars;
    }

    public int getExpectedStatus() {
        return expectedStatus;
    }

    public void setExpectedStatus(int expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    public String getAssertions() {
        return assertions;
    }

    public void setAssertions(String assertions) {
        this.assertions = assertions;
    }

    public boolean isSaveResponse() {
        return saveResponse;
    }

    public void setSaveResponse(boolean saveResponse) {
        this.saveResponse = saveResponse;
    }
} 