package com.yuriytkach.batb.cdk;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyProps;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.Version;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class BankAccountsTrackStack extends Stack {

  public BankAccountsTrackStack(final Construct parent, final String id) {
    this(parent, id, null);
  }

  public BankAccountsTrackStack(final Construct parent, final String id, final StackProps props) {
    super(parent, id, props);

    final var lambdaBankStatusUpdater = createLambda();

    final var lambdaAlias = createVersionAndUpdateAlias(lambdaBankStatusUpdater);

    //createRunSchedule(lambdaAlias);

    addSsmReadPermissions(lambdaBankStatusUpdater);
  }

  private void addSsmReadPermissions(final Function lambdaBankStatusUpdater) {
    // Retrieve properties
    String prefix = "batb";
    String tokens = "tokens";
    String accounts = "accounts";
    String statuses = "statuses";

    PolicyStatement readPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParametersByPath"))
      .resources(
        Stream.of(tokens, accounts)
          .flatMap(suffix ->
              Stream.of(
                "arn:aws:ssm:%s:%s:parameter/%s/*/%s",
                "arn:aws:ssm:%s:%s:parameter/%s/*/%s/",
                "arn:aws:ssm:%s:%s:parameter/%s/*/%s/*"
              ).map(pattern -> pattern.formatted(
                Stack.of(this).getRegion(),
                Stack.of(this).getAccount(),
                prefix,
                suffix
              ))
          )
            .toList())
      .build();

    PolicyStatement writePolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:PutParameter"))
      .resources(
        Stream.of(
          "arn:aws:ssm:%s:%s:parameter/%s/*/%s",
          "arn:aws:ssm:%s:%s:parameter/%s/*/%s/",
          "arn:aws:ssm:%s:%s:parameter/%s/*/%s/*"
        ).map(pattern -> pattern.formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount(),
          prefix,
          statuses
        ))
          .toList())
      .build();

    lambdaBankStatusUpdater.getRole().attachInlinePolicy(
      new Policy(this, "ParameterStorePolicy", PolicyProps.builder()
        .statements(Arrays.asList(readPolicy, writePolicy))
        .build())
    );
  }

  private void createRunSchedule(final Alias lambdaAlias) {
    final var everyFiveMinutesRule = Rule.Builder.create(this, "BankStatusUpdaterRule")
      .schedule(Schedule.expression("rate(5 minutes)"))
      .build();

    everyFiveMinutesRule.addTarget(new LambdaFunction(lambdaAlias));
  }

  private Alias createVersionAndUpdateAlias(final Function lambdaBankStatusUpdater) {
    final var lambdaVersion = Version.Builder.create(this, "LambdaVersion")
      .lambda(lambdaBankStatusUpdater)
      .description("New lambda version")
      .removalPolicy(RemovalPolicy.DESTROY)
      .build();

    final var lambdaAlias = Alias.Builder.create(this, "LambdaAlias")
      .aliasName("prod")
      .version(lambdaVersion)
      .build();
    return lambdaAlias;
  }

  private Function createLambda() {
    final var lambdaBankStatusUpdater = Function.Builder.create(this, "BankStatusUpdater")
      .runtime(Runtime.JAVA_17)
      .code(Code.fromAsset("../lambda-bank-status-updater/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_DAY)
      .memorySize(256)
      .timeout(Duration.minutes(10))
      .build();
    return lambdaBankStatusUpdater;
  }

}
