import { Assertion, AssertionResult } from '../interfaces';
import { JSONPath } from 'jsonpath-plus';
import Ajv from 'ajv';
import { Client } from 'elasticsearch';
import { Knex } from 'knex';
import { VariableManager } from './VariableManager';

export class AssertionEngine {
  private ajv: Ajv;
  private esClient?: Client;
  private dbClient?: Knex;

  constructor(
    private variableManager: VariableManager,
    esConfig?: any,
    dbConfig?: any
  ) {
    this.ajv = new Ajv();
    if (esConfig) {
      this.esClient = new Client(esConfig);
    }
    if (dbConfig) {
      this.dbClient = Knex(dbConfig);
    }
  }

  async validateAssertion(assertion: Assertion, response: any): Promise<AssertionResult> {
    try {
      switch (assertion.validationType) {
        case 'database':
          return await this.validateDatabaseAssertion(assertion);
        case 'responseBody':
          return this.validateResponseBodyAssertion(assertion, response);
        case 'ResponseMetaType':
          return this.validateResponseMetaAssertion(assertion, response);
        case 'Kibana':
          return await this.validateKibanaAssertion(assertion);
        case 'jsonSchema':
          return this.validateJsonSchemaAssertion(assertion, response);
        default:
          throw new Error(`Unknown validation type: ${assertion.validationType}`);
      }
    } catch (error) {
      return {
        assertionID: assertion.assertionID,
        status: 'failed',
        expected: assertion.expected,
        actual: 'Error during validation',
        error: error.message
      };
    }
  }

  private async validateDatabaseAssertion(assertion: Assertion): Promise<AssertionResult> {
    if (!this.dbClient) {
      throw new Error('Database client not configured');
    }

    const query = this.variableManager.substituteVariables(assertion.actual);
    const result = await this.dbClient.raw(query);
    const expected = this.variableManager.substituteVariables(assertion.expected);

    return {
      assertionID: assertion.assertionID,
      status: this.compareValues(result.rows, expected) ? 'passed' : 'failed',
      expected,
      actual: result.rows
    };
  }

  private validateResponseBodyAssertion(assertion: Assertion, response: any): AssertionResult {
    const jsonPath = this.variableManager.substituteVariables(assertion.actual);
    const actualValue = JSONPath({ path: jsonPath, json: response });
    const expectedValue = this.variableManager.substituteVariables(assertion.expected);

    return {
      assertionID: assertion.assertionID,
      status: this.compareValues(actualValue[0], expectedValue) ? 'passed' : 'failed',
      expected: expectedValue,
      actual: actualValue[0]
    };
  }

  private validateResponseMetaAssertion(assertion: Assertion, response: any): AssertionResult {
    const property = this.variableManager.substituteVariables(assertion.actual);
    const actualValue = response[property];
    const expectedValue = this.variableManager.substituteVariables(assertion.expected);

    return {
      assertionID: assertion.assertionID,
      status: this.compareValues(actualValue, expectedValue) ? 'passed' : 'failed',
      expected: expectedValue,
      actual: actualValue
    };
  }

  private async validateKibanaAssertion(assertion: Assertion): Promise<AssertionResult> {
    if (!this.esClient) {
      throw new Error('Elasticsearch client not configured');
    }

    const query = JSON.parse(this.variableManager.substituteVariables(assertion.actual));
    const result = await this.esClient.search(query);
    const expected = this.variableManager.substituteVariables(assertion.expected);

    return {
      assertionID: assertion.assertionID,
      status: this.compareValues(result.hits.hits, expected) ? 'passed' : 'failed',
      expected,
      actual: result.hits.hits
    };
  }

  private validateJsonSchemaAssertion(assertion: Assertion, response: any): AssertionResult {
    const schema = JSON.parse(this.variableManager.substituteVariables(assertion.actual));
    const validate = this.ajv.compile(schema);
    const isValid = validate(response);

    return {
      assertionID: assertion.assertionID,
      status: isValid ? 'passed' : 'failed',
      expected: 'JSON Schema validation',
      actual: isValid ? 'Valid' : `Invalid: ${this.ajv.errorsText(validate.errors)}`
    };
  }

  private compareValues(actual: any, expected: any): boolean {
    if (typeof expected === 'string' && expected.startsWith('regex:')) {
      const regex = new RegExp(expected.substring(6));
      return regex.test(actual);
    }

    if (typeof actual === 'object' && typeof expected === 'object') {
      return JSON.stringify(actual) === JSON.stringify(expected);
    }

    return actual == expected;
  }
} 