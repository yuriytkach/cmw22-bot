# Bank Accounts Track Bot

This project is a multi-module Gradle application designed to provide various services related 
to tracking bank accounts and integrating with different platforms like Telegram. 
The project is built using Java 21 and Quarkus, and includes several Lambda functions deployed on AWS using the AWS CDK.

## Project Structure

The project is organized into the following modules:

- **cdk**: Contains AWS Cloud Development Kit (CDK) scripts for deploying the infrastructure.
- **common**: Contains shared utilities and common logic used across other modules.
- **lambda-bank-status-updater**: Lambda function responsible for updating the bank account status.
- **lambda-telegram-bot**: Lambda function that integrates with Telegram to provide bot services.
- **lambda-bot-authorizer**: Lambda function that handles Telegram bot authorization.
- **lambda-status-api**: Provides API endpoints for querying the status of bank accounts.
- **lambda-donators-reporter**: Lambda function that generates monthly reports about donators.
- **lambda-ai-service**: Provides AI-powered services to other lambdas.
- **lambda-birthday-checker**: Lambda function to check for and report birthdays.

## Build and Run

### Prerequisites

- **Java 21**: Make sure you have JDK 21 installed and configured.
- **AWS CLI**: Ensure you have the AWS CLI installed and configured.
- **Docker**: The project can use Docker for building native executables.
- **GraalVM**: If you want to build native executables without Docker.

### Building the Project

To build the entire project, run:

```bash
./gradlew clean build
```

This will compile the code, run the tests, and package the applications.

### Running Tests

You can run the tests using the following command:

```bash
./gradlew test
```

### Deployment

Deployment is managed via AWS CDK, which is defined in the `cdk` module. 
Make sure you have AWS CLI configured and the required permissions set up.

View the README in the `cdk` module for more information on deploying the infrastructure.

### Dependencies

Each module can declare its dependencies. For instance, most modules depend on the `common` module for shared logic. 
Quarkus dependencies are managed centrally via the enforced platform BOM.


## Contribution

If you wish to contribute to the project, please fork the repository, create a new branch, and submit a pull request. 
Ensure that all code follows the established style guidelines and includes tests where appropriate.
