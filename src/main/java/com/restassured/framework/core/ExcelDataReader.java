package com.restassured.framework.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restassured.framework.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ExcelDataReader {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataReader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private Map<String, TestCase> testCases;
    private Map<String, TestStep> testSteps;
    private Map<String, List<Assertion>> assertions;
    private Map<String, List<com.restassured.framework.model.Header>> headers;
    private Map<String, String> bodies;
    private Map<String, List<TestData>> testData;
    
    public TestSuite readTestSuite(File file) throws IOException {
        if (file.getName().endsWith(".csv")) {
            return readFromCSV(file);
        } else {
            return readFromExcel(file);
        }
    }
    
    private TestSuite readFromCSV(File file) throws IOException {
        logger.info("Reading test suite from CSV file: {}", file.getAbsolutePath());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentTable = null;
            List<String> headers = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.endsWith("Table 1")) {
                    currentTable = line.split(":")[0].trim();
                    headers = null;
                    continue;
                }
                
                if (headers == null) {
                    headers = Arrays.asList(line.split(","));
                    continue;
                }
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.size() && i < values.length; i++) {
                    String value = values[i].trim();
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    record.put(headers.get(i), value);
                }
                
                switch (currentTable) {
                    case "TestCases":
                        parseTestCaseFromMap(record);
                        break;
                    case "TestSteps":
                        parseTestStepFromMap(record);
                        break;
                    case "Assertions":
                        parseAssertionFromMap(record);
                        break;
                    case "Headers":
                        parseHeaderFromMap(record);
                        break;
                    case "Bodies":
                        parseBodyFromMap(record);
                        break;
                    case "TestData":
                        parseTestDataFromMap(record);
                        break;
                }
            }
        }
        
        return buildTestSuite();
    }
    
    private TestSuite readFromExcel(File file) throws IOException {
        logger.info("Reading test suite from Excel file: {}", file.getAbsolutePath());
        
        try (Workbook workbook = WorkbookFactory.create(file)) {
            for (Sheet sheet : workbook) {
                String sheetName = sheet.getSheetName();
                Iterator<Row> rowIterator = sheet.iterator();
                
                if (!rowIterator.hasNext()) continue;
                Row headerRow = rowIterator.next();
                
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    switch (sheetName) {
                        case "TestCases":
                            parseTestCaseFromExcel(row);
                            break;
                        case "TestSteps":
                            parseTestStepFromExcel(row);
                            break;
                        case "Assertions":
                            parseAssertionFromExcel(row);
                            break;
                        case "Headers":
                            parseHeaderFromExcel(row);
                            break;
                        case "Bodies":
                            parseBodyFromExcel(row);
                            break;
                        case "TestData":
                            parseTestDataFromExcel(row);
                            break;
                    }
                }
            }
        }
        
        return buildTestSuite();
    }
    
    private void parseTestCaseFromMap(Map<String, String> record) {
        TestCase testCase = new TestCase();
        testCase.setTestCaseId(record.get("TestCaseID"));
        testCase.setTestCaseName(record.get("TestCaseName"));
        testCase.setDescription(record.get("Description"));
        testCase.setExecute("Yes".equalsIgnoreCase(record.get("Execute")));
        testCase.setPreRequisite(record.get("PreRequisite"));
        testCase.setTestSteps(Arrays.asList(record.get("TestSteps").split(",\\s*")));
        
        if (testCases == null) {
            testCases = new HashMap<>();
        }
        testCases.put(testCase.getTestCaseId(), testCase);
    }
    
    private void parseTestCaseFromExcel(Row row) {
        TestCase testCase = new TestCase();
        testCase.setTestCaseId(getCellValue(row.getCell(0)));
        testCase.setTestCaseName(getCellValue(row.getCell(1)));
        testCase.setDescription(getCellValue(row.getCell(2)));
        testCase.setExecute("Yes".equalsIgnoreCase(getCellValue(row.getCell(3))));
        testCase.setPreRequisite(getCellValue(row.getCell(4)));
        testCase.setTestSteps(Arrays.asList(getCellValue(row.getCell(5)).split(",\\s*")));
        
        if (testCases == null) {
            testCases = new HashMap<>();
        }
        testCases.put(testCase.getTestCaseId(), testCase);
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private void parseTestStepFromMap(Map<String, String> record) {
        TestStep testStep = new TestStep();
        testStep.setStepId(record.get("StepID"));
        testStep.setApiName(record.get("APIName"));
        testStep.setMethod(record.get("Method"));
        testStep.setEndpoint(record.get("Endpoint"));
        testStep.setHeaders(record.get("Headers"));
        testStep.setBody(record.get("Body"));
        testStep.setExtractVars(record.get("ExtractVars"));
        
        String expectedStatus = record.get("ExpectedStatus");
        try {
            testStep.setExpectedStatus(expectedStatus != null && !expectedStatus.isEmpty() 
                ? Integer.parseInt(expectedStatus) 
                : 200);
        } catch (NumberFormatException e) {
            logger.warn("Invalid expected status: {}. Using default value 200.", expectedStatus);
            testStep.setExpectedStatus(200);
        }
        
        testStep.setAssertions(record.get("Assertions"));
        testStep.setSaveResponse("Yes".equalsIgnoreCase(record.get("SaveResponse")));
        
        if (testSteps == null) {
            testSteps = new HashMap<>();
        }
        testSteps.put(testStep.getStepId(), testStep);
    }
    
    private void parseTestStepFromExcel(Row row) {
        TestStep testStep = new TestStep();
        testStep.setStepId(getCellValue(row.getCell(0)));
        testStep.setApiName(getCellValue(row.getCell(1)));
        testStep.setMethod(getCellValue(row.getCell(2)));
        testStep.setEndpoint(getCellValue(row.getCell(3)));
        testStep.setHeaders(getCellValue(row.getCell(4)));
        testStep.setBody(getCellValue(row.getCell(5)));
        testStep.setExtractVars(getCellValue(row.getCell(6)));
        testStep.setExpectedStatus(Integer.parseInt(getCellValue(row.getCell(7))));
        testStep.setAssertions(getCellValue(row.getCell(8)));
        testStep.setSaveResponse("Yes".equalsIgnoreCase(getCellValue(row.getCell(9))));
        
        if (testSteps == null) {
            testSteps = new HashMap<>();
        }
        testSteps.put(testStep.getStepId(), testStep);
    }
    
    private void parseAssertionFromMap(Map<String, String> record) {
        Assertion assertion = new Assertion();
        assertion.setAssertionId(record.get("Assertions"));
        assertion.setValidationType(record.get("Validation_Type"));
        assertion.setValidationInputType(record.get("Validation_input_Type"));
        assertion.setExpected(record.get("Expected"));
        assertion.setActual(record.get("Actual"));
        
        if (assertions == null) {
            assertions = new HashMap<>();
        }
        assertions.computeIfAbsent(assertion.getAssertionId(), k -> new ArrayList<>()).add(assertion);
    }
    
    private void parseAssertionFromExcel(Row row) {
        Assertion assertion = new Assertion();
        assertion.setAssertionId(getCellValue(row.getCell(0)));
        assertion.setValidationType(getCellValue(row.getCell(1)));
        assertion.setValidationInputType(getCellValue(row.getCell(2)));
        assertion.setExpected(getCellValue(row.getCell(3)));
        assertion.setActual(getCellValue(row.getCell(4)));
        
        if (assertions == null) {
            assertions = new HashMap<>();
        }
        assertions.computeIfAbsent(assertion.getAssertionId(), k -> new ArrayList<>()).add(assertion);
    }
    
    private void parseHeaderFromMap(Map<String, String> record) {
        com.restassured.framework.model.Header header = new com.restassured.framework.model.Header();
        header.setHeaderId(record.get("HeaderID"));
        header.setKey(record.get("Key"));
        header.setValue(record.get("Value"));
        
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.computeIfAbsent(header.getHeaderId(), k -> new ArrayList<>()).add(header);
    }
    
    private void parseHeaderFromExcel(Row row) {
        com.restassured.framework.model.Header header = new com.restassured.framework.model.Header();
        header.setHeaderId(getCellValue(row.getCell(0)));
        header.setKey(getCellValue(row.getCell(1)));
        header.setValue(getCellValue(row.getCell(2)));
        
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.computeIfAbsent(header.getHeaderId(), k -> new ArrayList<>()).add(header);
    }
    
    private void parseBodyFromMap(Map<String, String> record) {
        if (bodies == null) {
            bodies = new HashMap<>();
        }
        bodies.put(record.get("BodyID"), record.get("JSONTemplate"));
    }
    
    private void parseBodyFromExcel(Row row) {
        if (bodies == null) {
            bodies = new HashMap<>();
        }
        bodies.put(getCellValue(row.getCell(0)), getCellValue(row.getCell(1)));
    }
    
    private void parseTestDataFromMap(Map<String, String> record) {
        TestData data = new TestData();
        data.setTestCaseId(record.get("TestCaseID"));
        data.setVarName(record.get("VarName"));
        data.setValue(record.get("Value"));
        
        if (testData == null) {
            testData = new HashMap<>();
        }
        testData.computeIfAbsent(data.getTestCaseId(), k -> new ArrayList<>()).add(data);
    }
    
    private void parseTestDataFromExcel(Row row) {
        TestData data = new TestData();
        data.setTestCaseId(getCellValue(row.getCell(0)));
        data.setVarName(getCellValue(row.getCell(1)));
        data.setValue(getCellValue(row.getCell(2)));
        
        if (testData == null) {
            testData = new HashMap<>();
        }
        testData.computeIfAbsent(data.getTestCaseId(), k -> new ArrayList<>()).add(data);
    }
    
    private TestSuite buildTestSuite() {
        TestSuite testSuite = new TestSuite();
        testSuite.setTestCases(testCases);
        testSuite.setTestSteps(testSteps);
        testSuite.setAssertions(assertions);
        testSuite.setHeaders(headers);
        testSuite.setBodies(bodies);
        testSuite.setTestData(testData);
        return testSuite;
    }
} 