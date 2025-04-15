# RestAssured API Testing Framework

A user-friendly framework for automated API testing that uses Excel/CSV files to define test cases. No coding knowledge required!

## 🌟 Features

- **No Coding Required**: Define all your API tests in simple Excel/CSV files
- **Easy to Use**: Simple table-based structure for test definitions
- **Powerful**: Supports complex API testing scenarios
- **Flexible**: Works with any REST API
- **Extensible**: Easy to add new features

## 📋 Prerequisites

Before you start, make sure you have:
- Java 17 or higher installed
- Basic understanding of API concepts (endpoints, requests, responses)
- Your API documentation ready

## 🚀 Quick Start Guide

### 1. Download and Setup

1. Download the framework files
2. Open the `restass_Excel_Driven_API_Framework.csv` file in Excel or any text editor

### 2. Understanding the Test File Structure

The test file is divided into several tables:

#### TestCases Table
- **TestCaseID**: Unique identifier for your test (e.g., TC_001)
- **TestCaseName**: Name of your test
- **Description**: What the test does
- **Execute**: Set to "Yes" to run the test
- **PreRequisite**: Any test that needs to run before this one
- **TestSteps**: List of steps to execute (e.g., "Step_001, Step_002")

#### TestSteps Table
- **StepID**: Unique identifier for each step
- **APIName**: Name of the API
- **Method**: HTTP method (GET, POST, PUT, DELETE)
- **Endpoint**: API endpoint URL
- **Headers**: Reference to headers table
- **Body**: Reference to request body
- **ExtractVars**: Variables to extract from response (e.g., "token=access_token")
- **ExpectedStatus**: Expected HTTP status code
- **Assertions**: Reference to assertions table
- **SaveResponse**: Whether to save the response (Yes/No)

#### Headers Table
- **HeaderID**: Reference ID for headers
- **Key**: Header name
- **Value**: Header value (use {{variable}} for dynamic values)

#### Bodies Table
- **BodyID**: Reference ID for request body
- **JSONTemplate**: JSON request body template

#### TestData Table
- **TestCaseID**: Reference to test case
- **VarName**: Variable name
- **Value**: Variable value

### 3. Creating Your First Test

Let's create a simple login test:

1. In the TestCases table, add:
```
TC_001,Login Test,Test user login functionality,Yes,None,Step_001
```

2. In the TestSteps table, add:
```
Step_001,Login,POST,/api/login,Header_001,Body_001,token=access_token,200,Assertions_001,Yes
```

3. In the Headers table, add:
```
Header_001,Content-Type,application/json
```

4. In the Bodies table, add:
```
Body_001,{"username": "testuser", "password": "testpass"}
```

### 4. Running Your Tests

1. Open terminal/command prompt
2. Navigate to the framework directory
3. Run: `./gradlew test`

## 📝 Common Use Cases

### 1. Simple API Call
```
TestSteps:
Step_001,GetUsers,GET,/api/users,Header_001,,,200,Assertions_001,Yes
```

### 2. API Call with Request Body
```
TestSteps:
Step_001,CreateUser,POST,/api/users,Header_001,Body_001,,201,Assertions_001,Yes
```

### 3. Chaining API Calls (Using Response from First Call)
```
TestSteps:
Step_001,Login,POST,/api/login,Header_001,Body_001,token=access_token,200,Assertions_001,Yes
Step_002,GetProfile,GET,/api/profile,Header_002,,,200,Assertions_002,Yes

Headers:
Header_002,Authorization,Bearer {{token}}
```

## 🔍 Tips and Tricks

1. **Variable Substitution**: Use `{{variableName}}` to use values from previous steps
2. **Response Extraction**: Use `variableName=jsonPath` to extract values from responses
3. **Assertions**: Define expected values in the Assertions table
4. **Test Data**: Store test data in the TestData table

## 🛠️ Troubleshooting

Common issues and solutions:

1. **Test Fails with 404**
   - Check your endpoint URL
   - Verify the API is running

2. **Test Fails with 401/403**
   - Check your authentication headers
   - Verify tokens are being extracted correctly

3. **Variable Not Found**
   - Check the ExtractVars syntax
   - Verify the JSON path is correct

## 📚 Additional Resources

- [RestAssured Documentation](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [JSON Path Syntax](https://github.com/json-path/JsonPath)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)

## 🤝 Contributing

Feel free to submit issues and enhancement requests!

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details. 