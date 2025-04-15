import { APIRequestContext, APIResponse } from '@playwright/test';
import { TestStep, Header, Body } from '../interfaces';
import { VariableManager } from './VariableManager';

export class PlaywrightRequestHandler {
  constructor(
    private requestContext: APIRequestContext,
    private variableManager: VariableManager
  ) {}

  async executeRequest(testStep: TestStep, headers: Header[], bodies: Body[]): Promise<APIResponse> {
    const method = testStep.method.toUpperCase();
    const endpoint = this.variableManager.substituteVariables(testStep.endpoint);
    
    // Prepare headers
    const requestHeaders = this.prepareHeaders(testStep.headers, headers);
    
    // Prepare body
    const requestBody = this.prepareBody(testStep.body, bodies);

    try {
      const response = await this.requestContext.fetch(endpoint, {
        method,
        headers: requestHeaders,
        data: requestBody,
        failOnStatusCode: false // We'll handle status code validation separately
      });

      return response;
    } catch (error) {
      throw new Error(`Failed to execute request: ${error.message}`);
    }
  }

  private prepareHeaders(headerRef: string | undefined, headers: Header[]): Record<string, string> {
    const requestHeaders: Record<string, string> = {};
    
    if (!headerRef) return requestHeaders;

    const headerIds = headerRef.split(',').map(id => id.trim());
    for (const headerId of headerIds) {
      const header = headers.find(h => h.headerID === headerId);
      if (header) {
        requestHeaders[header.key] = this.variableManager.substituteVariables(header.value);
      }
    }

    return requestHeaders;
  }

  private prepareBody(bodyRef: string | undefined, bodies: Body[]): any {
    if (!bodyRef) return undefined;

    const body = bodies.find(b => b.bodyID === bodyRef);
    if (!body) {
      throw new Error(`Body template ${bodyRef} not found`);
    }

    const bodyTemplate = this.variableManager.substituteVariables(body.jsonTemplate);
    try {
      return JSON.parse(bodyTemplate);
    } catch (error) {
      throw new Error(`Invalid JSON template in body ${bodyRef}: ${error.message}`);
    }
  }

  async handleMultipartFormData(
    endpoint: string,
    formData: Record<string, string | { name: string; mimeType: string; buffer: Buffer }>
  ): Promise<APIResponse> {
    const form = new FormData();
    
    for (const [key, value] of Object.entries(formData)) {
      if (typeof value === 'string') {
        form.append(key, value);
      } else {
        form.append(key, new Blob([value.buffer], { type: value.mimeType }), value.name);
      }
    }

    return this.requestContext.fetch(endpoint, {
      method: 'POST',
      body: form
    });
  }
} 