# Playwright API Testing Framework

This is a TypeScript-based API testing framework using Playwright, designed to work alongside the existing RestAssured framework.

## Features

- TypeScript support for type-safe API testing
- CSV-based test definitions
- Mock server for testing
- Parallel test execution
- HTML test reports
- Environment variable support

## Prerequisites

- Node.js 16 or higher
- npm or yarn
- TypeScript

## Installation

1. Clone the repository:
```bash
git clone https://github.com/KarthikTools/restAssuredFW.git
cd restAssuredFW
git checkout playwright
```

2. Install dependencies:
```bash
npm install
```

3. Install Playwright browsers:
```bash
npx playwright install
```

## Project Structure

```
playwright/
├── src/
│   └── server.ts        # Mock API server
├── tests/
│   └── user.test.ts     # API test definitions
├── test_definitions/
│   └── sample_test.csv  # CSV test definitions
├── playwright.config.ts # Playwright configuration
├── tsconfig.json        # TypeScript configuration
└── package.json         # Project dependencies
```

## Running Tests

1. Start the mock server:
```bash
npx ts-node src/server.ts
```

2. Run the tests:
```bash
npx playwright test
```

3. View test reports:
```bash
npx playwright show-report
```

## Test Definition Format

Tests are defined in CSV format with the following tables:

1. TestCases
2. TestSteps
3. Assertions
4. Headers
5. Bodies
6. TestData

See `test_definitions/sample_test.csv` for an example.

## Configuration

- Environment variables are loaded from `.env` file
- Test configuration is in `playwright.config.ts`
- TypeScript settings are in `tsconfig.json`

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Submit a pull request

## License

MIT License 