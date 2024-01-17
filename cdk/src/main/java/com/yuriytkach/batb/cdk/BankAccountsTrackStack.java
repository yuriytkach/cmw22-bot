package com.yuriytkach.batb.cdk;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.MethodLoggingLevel;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.RequestAuthorizer;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Policy;
import software.amazon.awscdk.services.iam.PolicyProps;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.PolicyStatementProps;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class BankAccountsTrackStack extends Stack {

  public BankAccountsTrackStack(final Construct parent, final String id) {
    this(parent, id, null);
  }

  public BankAccountsTrackStack(final Construct parent, final String id, final StackProps props) {
    super(parent, id, props);

    final var lambdaStatusUpdaterAlias = createUpdaterLambda();
    createRunSchedule(lambdaStatusUpdaterAlias);

    final var lambdaBotAuthorizerAlias = createBotAuthorizerLambda();

    final var lambdaTelegramBotAlias = createTelegramBotLambda();

    final RequestAuthorizer authorizer = RequestAuthorizer.Builder.create(this, "TelegramAuthorizerBot")
      .handler(lambdaBotAuthorizerAlias)
      .identitySources(List.of("method.request.header.X-Telegram-Bot-Api-Secret-Token"))
      .resultsCacheTtl(Duration.hours(1))
      .build();

    final LogGroup apiLogGroup = LogGroup.Builder.create(this, "ApiGatewayLogGroup")
      .logGroupName("/aws/apigateway/TelegramBotApi")
      .removalPolicy(RemovalPolicy.DESTROY)
      .retention(RetentionDays.ONE_DAY)
      .build();

    final RestApi restApi = RestApi.Builder.create(this, "TelegramBotApi")
      .restApiName("TelegramBotApi")
      .cloudWatchRole(true)
      .cloudWatchRoleRemovalPolicy(RemovalPolicy.DESTROY)
      .deployOptions(StageOptions.builder()
        .stageName("prod")
        .loggingLevel(MethodLoggingLevel.INFO)
        .accessLogDestination(new LogGroupLogDestination(apiLogGroup))
        .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
        .build())
      .build();

    final IResource hookResource = restApi.getRoot().addResource("hook");
    final Resource eventResource = hookResource.addResource("event");
    eventResource.addMethod("POST", new LambdaIntegration(lambdaTelegramBotAlias), MethodOptions.builder()
      .authorizer(authorizer)
      .authorizationType(AuthorizationType.CUSTOM)
      .build());
  }

  private void addSsmReadPermissions(final Function lambda, final String id) {
    // Retrieve properties
    String prefix = "batb";
    String tokens = "tokens";
    String accounts = "accounts";
    String statuses = "statuses";

    PolicyStatement readPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParametersByPath"))
      .resources(
        Stream.of(tokens)
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

    PolicyStatement readPolicy2 = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParameter"))
      .resources(List.of(
        "arn:aws:ssm:%s:%s:parameter/%s/*/%s".formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount(),
          prefix,
          accounts
        ))
      )
      .build();

    PolicyStatement writeAndReadStatusesPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:PutParameter"))
      .resources(
        Stream.of(
            "arn:aws:ssm:%s:%s:parameter/%s/%s",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/*"
          ).map(pattern -> pattern.formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount(),
            prefix,
            statuses
          ))
          .toList())
      .build();

    lambda.getRole().attachInlinePolicy(
      new Policy(this, id + "ParameterStorePolicy", PolicyProps.builder()
        .statements(Arrays.asList(readPolicy, readPolicy2, writeAndReadStatusesPolicy))
        .build())
    );
  }

  private void createRunSchedule(final Alias lambdaAlias) {
    final var everyFiveMinutesRule = Rule.Builder.create(this, "BankStatusUpdaterRule")
      .schedule(Schedule.rate(Duration.hours(2)))
      .build();

    everyFiveMinutesRule.addTarget(new LambdaFunction(lambdaAlias));
  }

  private Alias createVersionAndUpdateAlias(final Function lambda, final String id) {
    final var lambdaCurrentVersion = lambda.getCurrentVersion();
    return Alias.Builder.create(this, id + "LambdaAlias")
      .aliasName("prod")
      .version(lambdaCurrentVersion)
      .build();
  }

  private Alias createUpdaterLambda() {
    final var lambda = Function.Builder.create(this, "BankStatusUpdater")
      .runtime(Runtime.JAVA_17)
      .code(Code.fromAsset("../lambda-bank-status-updater/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_DAY)
      .memorySize(256)
      .timeout(Duration.minutes(10))
      .build();

    addSsmReadPermissions(lambda, "BankStatusUpdater");

    return createVersionAndUpdateAlias(lambda, "BankStatusUpdater");
  }

  private Alias createTelegramBotLambda() {
    final var lambda = Function.Builder.create(this, "BankTelegramBot")
      .runtime(Runtime.JAVA_17)
      .code(Code.fromAsset("../lambda-telegram-bot/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_DAY)
      .memorySize(256)
      .timeout(Duration.seconds(25))
      .build();

    final String secretParamKey = "/bot/telegram/chat";
    final String tokenParamKey = "/bot/telegram/token";

    String prefix = "batb";
    String statuses = "statuses";

    PolicyStatement writeAndReadStatusesPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParametersByPath"))
      .resources(
        Stream.of(
            "arn:aws:ssm:%s:%s:parameter/%s/%s",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/*"
          ).map(pattern -> pattern.formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount(),
            prefix,
            statuses
          ))
          .toList())
      .build();

    final PolicyStatement readSecretPolicy = new PolicyStatement(PolicyStatementProps.builder()
      .actions(List.of("ssm:GetParameter"))
      .resources(List.of(
        "arn:aws:ssm:" + this.getRegion() + ":" + this.getAccount() + ":parameter" + secretParamKey,
        "arn:aws:ssm:" + this.getRegion() + ":" + this.getAccount() + ":parameter" + tokenParamKey
      ))
      .build());

    lambda.getRole().attachInlinePolicy(
      new Policy(this, "BankTelegramBot" + "ParameterStorePolicy", PolicyProps.builder()
        .statements(Arrays.asList(readSecretPolicy, writeAndReadStatusesPolicy))
        .build())
    );

    return createVersionAndUpdateAlias(lambda, "BankTelegramBot");
  }

  private Alias createBotAuthorizerLambda() {
    final var lambda = Function.Builder.create(this, "BotAuthorizer")
      .runtime(Runtime.PROVIDED_AL2023)
      .code(Code.fromAsset("../lambda-bot-authorizer/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_DAY)
      .memorySize(128)
      .timeout(Duration.seconds(10))
      .build();

    final String secretParamKey = "/bot/telegram/secret";

    lambda.addToRolePolicy(new PolicyStatement(PolicyStatementProps.builder()
      .actions(List.of("ssm:GetParameter"))
      .resources(List.of("arn:aws:ssm:" + this.getRegion() + ":" + this.getAccount() + ":parameter" + secretParamKey))
      .build()));

    return createVersionAndUpdateAlias(lambda, "BotAuthorizer");
  }

}
