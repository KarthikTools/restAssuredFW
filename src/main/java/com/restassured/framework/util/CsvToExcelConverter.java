package com.restassured.framework.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvToExcelConverter {
    private static final Logger logger = LoggerFactory.getLogger(CsvToExcelConverter.class);

    public static void convertCsvToExcel(String csvFilePath, String excelFilePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet currentSheet = null;
            List<String> headers = null;
            String currentTable = null;

            BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
            String line;
            int rowNum = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    rowNum = 0;
                    continue;
                }

                if (line.endsWith("Table 1")) {
                    currentTable = line.split(":")[0].trim();
                    currentSheet = workbook.createSheet(currentTable);
                    headers = null;
                    rowNum = 0;
                    continue;
                }

                if (headers == null) {
                    headers = parseCSVLine(line);
                    Row headerRow = currentSheet.createRow(rowNum++);
                    for (int i = 0; i < headers.size(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers.get(i));
                    }
                    continue;
                }

                List<String> values = parseCSVLine(line);
                Row row = currentSheet.createRow(rowNum++);
                for (int i = 0; i < values.size(); i++) {
                    Cell cell = row.createCell(i);
                    String value = values.get(i);
                    
                    // Try to parse as number if possible
                    try {
                        if (value.matches("\\d+")) {
                            cell.setCellValue(Integer.parseInt(value));
                        } else if (value.matches("\\d+\\.\\d+")) {
                            cell.setCellValue(Double.parseDouble(value));
                        } else {
                            cell.setCellValue(value);
                        }
                    } catch (NumberFormatException e) {
                        cell.setCellValue(value);
                    }
                }

                // Auto-size columns
                if (rowNum == 2) {
                    for (int i = 0; i < values.size(); i++) {
                        currentSheet.autoSizeColumn(i);
                    }
                }
            }

            // Write the workbook to file
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
            }

            logger.info("Successfully converted CSV to Excel: {}", excelFilePath);

        } catch (IOException e) {
            logger.error("Error converting CSV to Excel", e);
            throw new RuntimeException("Failed to convert CSV to Excel", e);
        }
    }

    private static List<String> parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());

        return values;
    }

    public static void main(String[] args) {
        String csvFile = "src/test/resources/sample_test.csv";
        String excelFile = "src/test/resources/sample_test.xlsx";
        convertCsvToExcel(csvFile, excelFile);
    }
} 