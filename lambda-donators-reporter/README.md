# Donators Reporter and Statistics Builder

Lambda is run periodically to generate a report of the top donators and their donations.
The data is taken from supported bank accounts (PrivatBank, Monobank Jars). 
The top donators are calculated based on the total amount of donations made by the donator.
The report is updated in Google Sheets.

Additionally, the lambda generates statistics of the donations made by the donators for the previous month.
The statistics is then sent as a Telegram message to the fund's group chat.

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
