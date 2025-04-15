package com.restassured.framework.model;

public class Assertion {
    private String assertionId;
    private String validationType;
    private String validationInputType;
    private String expected;
    private String actual;

    public String getAssertionId() {
        return assertionId;
    }

    public void setAssertionId(String assertionId) {
        this.assertionId = assertionId;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getValidationInputType() {
        return validationInputType;
    }

    public void setValidationInputType(String validationInputType) {
        this.validationInputType = validationInputType;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }
} 