import { test as base, expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';
import dotenv from 'dotenv';

dotenv.config();

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';

// Extend the test type to include typed request context
type TestFixtures = {
  request: APIRequestContext;
};

const test = base.extend<TestFixtures>({});

test.describe('User API Tests', () => {
  let userId: string;
  const testData = {
    name: 'John Doe',
    email: 'john.doe@example.com',
    password: 'secret123',
    activationCode: '123456',
    newName: 'Jane Doe',
    newEmail: 'jane.doe@example.com',
    newPassword: 'newSecret123'
  };

  test('TC_001: Create and Activate User', async ({ request }) => {
    // Step 1: Create User
    const createResponse = await request.post(`${BASE_URL}/api/users`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test-token'
      },
      data: {
        name: testData.name,
        email: testData.email,
        password: testData.password
      }
    });
    
    expect(createResponse.ok()).toBeTruthy();
    expect(createResponse.status()).toBe(201);
    const createResponseBody = await createResponse.json();
    userId = createResponseBody.id;
    
    // Step 2: Activate User
    const activateResponse = await request.post(`${BASE_URL}/api/users/${userId}/activate`, {
      headers: {
        'Authorization': 'Bearer test-token'
      },
      data: {
        activationCode: testData.activationCode
      }
    });
    
    expect(activateResponse.ok()).toBeTruthy();
    expect(activateResponse.status()).toBe(200);
  });

  test('TC_003: Update User Details', async ({ request }) => {
    test.fail(!userId, 'User ID is required for this test. Run TC_001 first.');

    // Step 4: Update User
    const updateResponse = await request.put(`${BASE_URL}/api/users/${userId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test-token'
      },
      data: {
        name: testData.newName,
        email: testData.newEmail,
        password: testData.newPassword
      }
    });
    
    expect(updateResponse.ok()).toBeTruthy();
    expect(updateResponse.status()).toBe(200);
    
    // Step 5: Verify Update
    const verifyResponse = await request.get(`${BASE_URL}/api/users/${userId}`);
    expect(verifyResponse.ok()).toBeTruthy();
    expect(verifyResponse.status()).toBe(200);
    const verifyResponseBody = await verifyResponse.json();
    expect(verifyResponseBody.name).toBe(testData.newName);
    expect(verifyResponseBody.email).toBe(testData.newEmail);
  });

  test('TC_004: Delete User', async ({ request }) => {
    test.fail(!userId, 'User ID is required for this test. Run TC_001 first.');

    // Step 6: Delete User
    const deleteResponse = await request.delete(`${BASE_URL}/api/users/${userId}`, {
      headers: {
        'Authorization': 'Bearer test-token'
      }
    });
    
    expect(deleteResponse.ok()).toBeTruthy();
    expect(deleteResponse.status()).toBe(204);
    
    // Step 7: Verify Delete
    const verifyResponse = await request.get(`${BASE_URL}/api/users/${userId}`);
    expect(verifyResponse.status()).toBe(404);
  });
}); 