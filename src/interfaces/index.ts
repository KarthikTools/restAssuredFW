export interface TestCase {
  testCaseID: string;
  testCaseName: string;
  description: string;
  execute: boolean;
  preRequisite?: string;
  testSteps: string[];
}

export interface TestStep {
  stepID: string;
  apiName: string;
  method: string;
  endpoint: string;
  headers?: string;
  body?: string;
  extractVars?: string;
  expectedStatus: number;
  assertions?: string;
  saveResponse: boolean;
}

export interface Assertion {
  assertionID: string;
  validationType: 'database' | 'responseBody' | 'ResponseMetaType' | 'Kibana' | 'jsonSchema';
  validationInputType: 'jsonPath' | 'Xpath' | 'sqlQuery' | 'schemaFile';
  expected: string;
  actual: string;
}

export interface Header {
  headerID: string;
  key: string;
  value: string;
}

export interface Body {
  bodyID: string;
  jsonTemplate: string;
}

export interface TestData {
  testCaseID: string;
  varName: string;
  value: string;
}

export interface VariableScope {
  global: Record<string, any>;
  testCase: Record<string, any>;
  step: Record<string, any>;
}

export interface TestResult {
  testCaseID: string;
  status: 'passed' | 'failed' | 'skipped';
  steps: StepResult[];
  error?: string;
}

export interface StepResult {
  stepID: string;
  status: 'passed' | 'failed' | 'skipped';
  request?: any;
  response?: any;
  assertions: AssertionResult[];
  error?: string;
}

export interface AssertionResult {
  assertionID: string;
  status: 'passed' | 'failed';
  expected: any;
  actual: any;
  error?: string;
} 