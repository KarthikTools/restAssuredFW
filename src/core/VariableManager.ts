import { VariableScope } from '../interfaces';
import { JSONPath } from 'jsonpath-plus';

export class VariableManager {
  private scope: VariableScope = {
    global: {},
    testCase: {},
    step: {}
  };

  constructor() {
    this.initializeGlobalFunctions();
  }

  private initializeGlobalFunctions(): void {
    this.scope.global['$randomUUID'] = () => crypto.randomUUID();
    this.scope.global['$timestamp'] = () => new Date().toISOString();
    this.scope.global['$randomInt'] = (min: number, max: number) => 
      Math.floor(Math.random() * (max - min + 1)) + min;
  }

  setGlobalVariable(name: string, value: any): void {
    this.scope.global[name] = value;
  }

  setTestCaseVariable(name: string, value: any): void {
    this.scope.testCase[name] = value;
  }

  setStepVariable(name: string, value: any): void {
    this.scope.step[name] = value;
  }

  getVariable(name: string): any {
    // Check in order: step -> testCase -> global
    if (name in this.scope.step) {
      return this.scope.step[name];
    }
    if (name in this.scope.testCase) {
      return this.scope.testCase[name];
    }
    if (name in this.scope.global) {
      return this.scope.global[name];
    }
    throw new Error(`Variable ${name} not found`);
  }

  substituteVariables(text: string): string {
    return text.replace(/\{\{([^}]+)\}\}/g, (match, variableName) => {
      try {
        const value = this.getVariable(variableName);
        return typeof value === 'function' ? value() : value;
      } catch (error) {
        return match; // Return original if variable not found
      }
    });
  }

  extractVariablesFromResponse(response: any, extractVars: string): void {
    if (!extractVars) return;

    const variables = extractVars.split(',').map(v => v.trim());
    for (const variable of variables) {
      const [varName, jsonPath] = variable.split('=').map(v => v.trim());
      if (!varName || !jsonPath) continue;

      try {
        const value = JSONPath({ path: jsonPath, json: response });
        this.setStepVariable(varName, value[0]);
      } catch (error) {
        console.error(`Error extracting variable ${varName} with path ${jsonPath}:`, error);
      }
    }
  }

  clearTestCaseScope(): void {
    this.scope.testCase = {};
  }

  clearStepScope(): void {
    this.scope.step = {};
  }

  getAllVariables(): VariableScope {
    return { ...this.scope };
  }
} 