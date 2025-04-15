import { ExcelDataReader } from './core/ExcelDataReader';
import { VariableManager } from './core/VariableManager';
import { PlaywrightRequestHandler } from './core/PlaywrightRequestHandler';
import { AssertionEngine } from './core/AssertionEngine';
import { TestExecutor } from './core/TestExecutor';
import { chromium } from '@playwright/test';
import * as dotenv from 'dotenv';
import { createLogger, format, transports } from 'winston';

// Load environment variables
dotenv.config();

// Configure logger
const logger = createLogger({
  level: 'info',
  format: format.combine(
    format.timestamp(),
    format.json()
  ),
  transports: [
    new transports.Console(),
    new transports.File({ filename: 'logs/error.log', level: 'error' }),
    new transports.File({ filename: 'logs/combined.log' })
  ]
});

async function main() {
  try {
    // Initialize components
    const dataReader = new ExcelDataReader();
    const variableManager = new VariableManager();
    
    // Read test definitions
    const filePath = process.env.TEST_DEFINITIONS_FILE || 'test_definitions.xlsx';
    await dataReader.readExcelFile(filePath);

    // Initialize Playwright
    const browser = await chromium.launch();
    const context = await browser.newContext();
    const requestContext = await context.request;

    // Initialize core components
    const requestHandler = new PlaywrightRequestHandler(requestContext, variableManager);
    const assertionEngine = new AssertionEngine(
      variableManager,
      process.env.ELASTICSEARCH_CONFIG ? JSON.parse(process.env.ELASTICSEARCH_CONFIG) : undefined,
      process.env.DATABASE_CONFIG ? JSON.parse(process.env.DATABASE_CONFIG) : undefined
    );

    const testExecutor = new TestExecutor(
      variableManager,
      requestHandler,
      assertionEngine,
      dataReader.getTestCases(),
      dataReader.getTestSteps(),
      dataReader.getHeaders(),
      dataReader.getBodies(),
      dataReader.getTestData()
    );

    // Execute tests
    logger.info('Starting test execution');
    const results = await testExecutor.executeTests();

    // Log results
    logger.info('Test execution completed', { results });

    // Generate report
    await generateReport(results);

    // Cleanup
    await browser.close();

  } catch (error) {
    logger.error('Error during test execution:', error);
    process.exit(1);
  }
}

async function generateReport(results: any[]): Promise<void> {
  // TODO: Implement report generation using Allure or Mochawesome
  console.log('Test Results:');
  results.forEach(result => {
    console.log(`\nTest Case: ${result.testCaseID}`);
    console.log(`Status: ${result.status}`);
    if (result.error) {
      console.log(`Error: ${result.error}`);
    }
    result.steps.forEach(step => {
      console.log(`\n  Step: ${step.stepID}`);
      console.log(`  Status: ${step.status}`);
      if (step.error) {
        console.log(`  Error: ${step.error}`);
      }
    });
  });
}

// Run the framework
main().catch(error => {
  logger.error('Unhandled error:', error);
  process.exit(1);
}); 