# CDK Module for Bank Accounts Track Bot Deployment

This module is part of a multi-module Gradle project and is responsible for deploying 
the Bank Accounts Track Bot application infrastructure using AWS Cloud Development Kit (CDK). 
The infrastructure includes several AWS Lambda functions, an API Gateway with custom domain setup, 
and IAM policies for secure access to resources.

## Prerequisites

Before deploying the infrastructure, ensure the following prerequisites are met:

1. **Build All Modules**: The project modules should be built before running the CDK deployment. 
This ensures that all required artifacts, such as Lambda function packages, are available for deployment.

   ```bash
   ./gradlew build
   ```

2. **AWS CLI Configuration**: Ensure your AWS CLI is configured with the necessary credentials and region settings.

   ```bash
   aws configure
   ```

3. **Install AWS CDK**: Ensure you have the AWS CDK installed globally.

   ```bash
   npm install -g aws-cdk
   ```

## What Does This CDK Module Do?

This CDK module sets up the following resources:

### 1. **Lambda Functions**

- **BankStatusUpdaterLambda**: Regularly updates the status of bank accounts.
- **TelegramBotLambda**: Manages interactions with a Telegram bot.
- **BotAuthorizerLambda**: Provides custom authorization for API Gateway based on Telegram bot credentials.
- **StatusApiLambda**: Exposes API endpoints for retrieving status and statistics.
- **AiServiceLambda**: Provides AI-based services, utilized by other Lambda functions.
- **DonatorsReporterLambda**: Generates reports on donations and triggers AI services.
- **BirthdayCheckerLambda**: Checks and processes birthdays using AI services.

### 2. **API Gateway**

- Sets up a REST API with endpoints secured by a custom authorizer.
- Configures CORS to allow specific origins (`https://www.cmw22.org`).
- Creates a custom domain (`cmw22-bot.yuriytkach.com`) and maps it to the API Gateway.

### 3. **EventBridge Rules**

- Schedules regular invocations of the Lambda functions to update statuses, generate reports, and check birthdays.

### 4. **IAM Policies**

- Grants Lambda functions the necessary permissions to access AWS SSM parameters and invoke other Lambda functions.

## How to Deploy

Once all prerequisites are met, you can deploy the CDK stack by following these steps:

1. **Bootstrap Your Environment** (if not already done):

   ```bash
   cdk bootstrap
   ```

2. **Deploy the Stack**:

   Navigate to the `cdk` module directory and run the CDK deploy command:

   ```bash
   cdk deploy
   ```

3. **Verify Deployment**:

   After deployment, verify that the resources are created correctly by checking the AWS Management Console.

## Cleanup

To remove the deployed resources, run the following command:

```bash
cdk destroy
```

Ensure that any data or resources you wish to retain are backed up before running the destroy command.
