import { Workbook } from 'exceljs';
import { createReadStream } from 'fs';
import { parse } from 'csv-parse';
import { TestCase, TestStep, Assertion, Header, Body, TestData } from '../interfaces';

export class ExcelDataReader {
  private testCases: TestCase[] = [];
  private testSteps: TestStep[] = [];
  private assertions: Assertion[] = [];
  private headers: Header[] = [];
  private bodies: Body[] = [];
  private testData: TestData[] = [];

  async readExcelFile(filePath: string): Promise<void> {
    const workbook = new Workbook();
    await workbook.xlsx.readFile(filePath);

    // Read each worksheet
    this.testCases = this.readWorksheet<TestCase>(workbook, 'TestCases');
    this.testSteps = this.readWorksheet<TestStep>(workbook, 'TestSteps');
    this.assertions = this.readWorksheet<Assertion>(workbook, 'Assertions');
    this.headers = this.readWorksheet<Header>(workbook, 'Headers');
    this.bodies = this.readWorksheet<Body>(workbook, 'Bodies');
    this.testData = this.readWorksheet<TestData>(workbook, 'TestData');
  }

  async readCSVFile(filePath: string): Promise<void> {
    const parser = createReadStream(filePath).pipe(parse({
      columns: true,
      skip_empty_lines: true
    }));

    for await (const record of parser) {
      // Determine which table this record belongs to based on the file name
      const tableName = this.getTableNameFromFilePath(filePath);
      this.addRecordToTable(record, tableName);
    }
  }

  private readWorksheet<T>(workbook: Workbook, sheetName: string): T[] {
    const worksheet = workbook.getWorksheet(sheetName);
    if (!worksheet) {
      throw new Error(`Worksheet ${sheetName} not found`);
    }

    const headers = worksheet.getRow(1).values as string[];
    const data: T[] = [];

    worksheet.eachRow((row, rowNumber) => {
      if (rowNumber === 1) return; // Skip header row

      const record: any = {};
      row.eachCell((cell, colNumber) => {
        const header = headers[colNumber];
        record[header] = cell.value;
      });
      data.push(record as T);
    });

    return data;
  }

  private getTableNameFromFilePath(filePath: string): string {
    const fileName = filePath.split('/').pop()?.split('.')[0] || '';
    return fileName.charAt(0).toUpperCase() + fileName.slice(1);
  }

  private addRecordToTable(record: any, tableName: string): void {
    switch (tableName) {
      case 'TestCases':
        this.testCases.push(record as TestCase);
        break;
      case 'TestSteps':
        this.testSteps.push(record as TestStep);
        break;
      case 'Assertions':
        this.assertions.push(record as Assertion);
        break;
      case 'Headers':
        this.headers.push(record as Header);
        break;
      case 'Bodies':
        this.bodies.push(record as Body);
        break;
      case 'TestData':
        this.testData.push(record as TestData);
        break;
      default:
        throw new Error(`Unknown table name: ${tableName}`);
    }
  }

  // Getters for accessing the data
  getTestCases(): TestCase[] {
    return this.testCases;
  }

  getTestSteps(): TestStep[] {
    return this.testSteps;
  }

  getAssertions(): Assertion[] {
    return this.assertions;
  }

  getHeaders(): Header[] {
    return this.headers;
  }

  getBodies(): Body[] {
    return this.bodies;
  }

  getTestData(): TestData[] {
    return this.testData;
  }
} 