# Lambda Bot Authorizer

## Description

The `lambda-bot-authorizer` is a Java-based module designed to authorize API Gateway requests in a secure and efficient 
manner. It leverages the power of AWS Lambda functions to perform its operations, providing a serverless solution 
that scales automatically according to the demand.

The module works by receiving an Authorizer request, processing it, and then generating a policy response. 
The policy response includes the effect (allow or deny), the resource ARN (Amazon Resource Name). 
This information is encapsulated in a `PolicyResponse` object, which is then returned to the caller.

This lambda is deployed with CDK to AWS in binary form, thus **should be compiled to a native image**.

## How it Works

The core functionality of the `lambda-bot-authorizer` module is encapsulated in the `createResponse` method. 
This method takes in two parameters: `effect` and `resourceArn`.

The `effect` parameter determines whether the request is allowed or denied access. The `resourceArn` parameter is the 
Amazon Resource Name of the resource that the resource is trying to access.

## Building the Module

The `lambda-bot-authorizer` module is a Gradle project, and it can be built using the Gradle Wrapper included 
in the root project. Here are the steps to build the module:

1. Open a terminal and navigate to the root directory of the project.
2. Run the following command to build the module as native executable:

```bash
./gradlew :lambda-bot-authorizer:build -Dquarkus.package.type=native
```

if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew :lambda-bot-authorizer:build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

This command compiles the Java code, runs the tests, and generates a `function.zip` file in the `build` directory.


## More Information
- AWS Lambda with Quarkus ([guide](https://quarkus.io/guides/amazon-lambda)): Write AWS Lambda functions
- Use API Gateway Lambda authorizers ([AWS Docs](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html))
