# Bank Status Updater

Lambda that is executed periodically to update the statuses of registered bank accounts of the fund.

The types of banks that are supported:
- PrivatBank
- Monobank Jars
- Google Sheet manual tracker

After checking all statuses lambda updates the Google Sheet registry with the new balances for all accounts.

Additionally, lambda updates the totals for current fund balance, that is used to display progress on the website.

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
