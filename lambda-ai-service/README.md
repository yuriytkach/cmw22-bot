# AI Services Lambda

Provides AI services for other lambdas. Uses langchain4j quarkus extension to call AI (ChatGpt).

The following AI services are provided:
- `TRANSLATE_NAMES` - Translates names from English to Ukrainian.
- `BIRTHDAY_WISH` - Generates a birthday wish for a person.
- `IMAGE_DONATION_STATS` - Generates an image for donation statistics for specific month.

The request to Lambda should be in the following format:
```json
{
  "service": "TRANSLATE_NAMES",
  "payload": "Stepan Bandera"
}
```

This lambda is deployed with CDK to AWS in binary form, thus **should be compiled to a native image**.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.
