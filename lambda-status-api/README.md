# Lambda Status API module

## Description

The `lambda-status-api` is a Java-based module that provides a RESTful API for retrieving the status of 
funds. It leverages the power of Quarkus to build serverless applications.

## How it Works

The module uses Quarkus' Amazon Lambda REST extension to expose RESTful endpoints that can be invoked via AWS Lambda. 
It also uses Quarkus' RESTEasy extension for building RESTful Web Services and Quarkus' RESTEasy Jackson extension 
for JSON binding.

The application's configuration is defined in the `application.yml` file, which includes settings for caching, 
logging, and other Quarkus features.

## Building and Running

The `lambda-status-api` module is a Gradle project, and it can be built using the Gradle Wrapper included
in the root project. Here are the steps to build the module:

1. Open a terminal and navigate to the root directory of the project.
2. Run the following command to build the module as native executable:

```bash
./gradlew :lambda-status-api:build -Dquarkus.package.type=native
```

if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew :lambda-status-api:build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

This command compiles the Java code, runs the tests, and generates a `function.zip` file in the `build` directory.


## More Information
- AWS Lambda Gateway REST API ([guide](https://quarkus.io/guides/amazon-lambda-http)): Build an API Gateway REST API with Lambda integration
- RESTEasy Classic ([guide](https://quarkus.io/guides/resteasy)): REST endpoint framework implementing JAX-RS and more
