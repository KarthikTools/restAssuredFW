import { TestCase, TestStep, TestResult, StepResult } from '../interfaces';
import { VariableManager } from './VariableManager';
import { PlaywrightRequestHandler } from './PlaywrightRequestHandler';
import { AssertionEngine } from './AssertionEngine';
import { createLogger, format, transports } from 'winston';

export class TestExecutor {
  private logger = createLogger({
    level: 'info',
    format: format.combine(
      format.timestamp(),
      format.json()
    ),
    transports: [
      new transports.File({ filename: 'logs/error.log', level: 'error' }),
      new transports.File({ filename: 'logs/combined.log' })
    ]
  });

  constructor(
    private variableManager: VariableManager,
    private requestHandler: PlaywrightRequestHandler,
    private assertionEngine: AssertionEngine,
    private testCases: TestCase[],
    private testSteps: TestStep[],
    private headers: any[],
    private bodies: any[],
    private testData: any[]
  ) {}

  async executeTests(): Promise<TestResult[]> {
    const results: TestResult[] = [];
    const executedTestCases = new Set<string>();

    for (const testCase of this.testCases) {
      if (!testCase.execute) {
        results.push({
          testCaseID: testCase.testCaseID,
          status: 'skipped',
          steps: []
        });
        continue;
      }

      // Check prerequisites
      if (testCase.preRequisite && !executedTestCases.has(testCase.preRequisite)) {
        const prerequisite = this.testCases.find(tc => tc.testCaseID === testCase.preRequisite);
        if (prerequisite) {
          const prerequisiteResult = await this.executeTestCase(prerequisite);
          results.push(prerequisiteResult);
          executedTestCases.add(prerequisite.testCaseID);
        }
      }

      const result = await this.executeTestCase(testCase);
      results.push(result);
      executedTestCases.add(testCase.testCaseID);
    }

    return results;
  }

  private async executeTestCase(testCase: TestCase): Promise<TestResult> {
    this.variableManager.clearTestCaseScope();
    this.loadTestData(testCase.testCaseID);

    const stepResults: StepResult[] = [];
    let testCaseStatus: 'passed' | 'failed' = 'passed';
    let error: string | undefined;

    try {
      for (const stepId of testCase.testSteps) {
        const step = this.testSteps.find(s => s.stepID === stepId);
        if (!step) {
          throw new Error(`Test step ${stepId} not found`);
        }

        const stepResult = await this.executeStep(step);
        stepResults.push(stepResult);

        if (stepResult.status === 'failed') {
          testCaseStatus = 'failed';
          error = stepResult.error;
          break;
        }
      }
    } catch (e) {
      testCaseStatus = 'failed';
      error = e.message;
      this.logger.error(`Error executing test case ${testCase.testCaseID}:`, e);
    }

    return {
      testCaseID: testCase.testCaseID,
      status: testCaseStatus,
      steps: stepResults,
      error
    };
  }

  private async executeStep(step: TestStep): Promise<StepResult> {
    this.variableManager.clearStepScope();

    try {
      const response = await this.requestHandler.executeRequest(step, this.headers, this.bodies);

      // Extract variables from response if specified
      if (step.extractVars) {
        this.variableManager.extractVariablesFromResponse(await response.json(), step.extractVars);
      }

      // Validate status code
      if (response.status() !== step.expectedStatus) {
        throw new Error(`Expected status ${step.expectedStatus}, got ${response.status()}`);
      }

      // Execute assertions if specified
      const assertionResults = [];
      if (step.assertions) {
        const assertionIds = step.assertions.split(',').map(id => id.trim());
        for (const assertionId of assertionIds) {
          const assertion = this.assertions.find(a => a.assertionID === assertionId);
          if (assertion) {
            const result = await this.assertionEngine.validateAssertion(assertion, await response.json());
            assertionResults.push(result);
          }
        }
      }

      return {
        stepID: step.stepID,
        status: assertionResults.every(r => r.status === 'passed') ? 'passed' : 'failed',
        request: {
          method: step.method,
          endpoint: step.endpoint,
          headers: step.headers,
          body: step.body
        },
        response: step.saveResponse ? await response.json() : undefined,
        assertions: assertionResults
      };
    } catch (error) {
      this.logger.error(`Error executing step ${step.stepID}:`, error);
      return {
        stepID: step.stepID,
        status: 'failed',
        assertions: [],
        error: error.message
      };
    }
  }

  private loadTestData(testCaseID: string): void {
    const testData = this.testData.filter(td => td.testCaseID === testCaseID);
    for (const data of testData) {
      this.variableManager.setTestCaseVariable(data.varName, data.value);
    }
  }
} 