package com.yuriytkach.batb.cdk;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import one.util.streamex.StreamEx;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AccessLogFormat;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.DomainName;
import software.amazon.awscdk.services.apigateway.DomainNameProps;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LogGroupLogDestination;
import software.amazon.awscdk.services.apigateway.MethodLoggingLevel;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.RequestAuthorizer;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.ResourceOptions;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.RuleTargetInput;
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
import software.amazon.awscdk.services.lambda.SnapStartConf;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class BankAccountsTrackStack extends Stack {

  private static final String CMW22_CORS_ORIGIN = "https://www.cmw22.org";

  public BankAccountsTrackStack(final Construct parent, final String id) {
    this(parent, id, null);
  }

  public BankAccountsTrackStack(final Construct parent, final String id, final StackProps props) {
    super(parent, id, props);

    createBankStatusUpdaterLambda();

    final var lambdaBotAuthorizerAlias = createBotAuthorizerLambda();

    final var lambdaTelegramBotAlias = createTelegramBotLambda();

    final var lambdaStatusApiAlias = createStatusApiLambda();

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

    final RestApi restApi = RestApi.Builder.create(this, "StatusApi")
      .restApiName("StatusApi")
      .cloudWatchRole(true)
      .cloudWatchRoleRemovalPolicy(RemovalPolicy.DESTROY)
      .deployOptions(StageOptions.builder()
        .stageName("prod")
        .loggingLevel(MethodLoggingLevel.INFO)
        .accessLogDestination(new LogGroupLogDestination(apiLogGroup))
        .accessLogFormat(AccessLogFormat.jsonWithStandardFields())
        .build())
      .build();

    customDomainForRestApi(restApi);

    final IResource hookResource = restApi.getRoot().addResource("hook");
    final Resource eventResource = hookResource.addResource("event");
    eventResource.addMethod("POST", new LambdaIntegration(lambdaTelegramBotAlias), MethodOptions.builder()
      .authorizer(authorizer)
      .authorizationType(AuthorizationType.CUSTOM)
      .build());

    final IResource statusResource = restApi.getRoot().addResource("status");
    final Resource fundraiserResource = statusResource.addResource(
      "{fundraiserId}",
      ResourceOptions.builder()
        .defaultCorsPreflightOptions(CorsOptions.builder()
          .allowMethods(List.of("GET,HEAD,OPTIONS"))
          .allowOrigins(List.of(CMW22_CORS_ORIGIN))
          .build())
        .build()
    );
    fundraiserResource.addMethod("GET", new LambdaIntegration(lambdaStatusApiAlias), MethodOptions.builder()
      .build());
  }

  private void addSsmReadPermissionsForBankStatusUpdaterLambda(final Function lambda, final String id) {
    // Retrieve properties
    String prefix = "batb";
    String tokens = "tokens";
    String accounts = "accounts";
    String statuses = "statuses";
    String funds = "funds";

    PolicyStatement readPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParametersByPath"))
      .resources(
        StreamEx.of(tokens)
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
          .append(Stream.of(
            "arn:aws:ssm:%s:%s:parameter/%s/%s",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/*"
          ).map(pattern -> pattern.formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount(),
            prefix,
            statuses
          )))
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
        ),
        "arn:aws:ssm:%s:%s:parameter/%s/gsheet/%s".formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount(),
          prefix,
          "registry"
        ),
        "arn:aws:ssm:%s:%s:parameter/%s/gsheet/%s".formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount(),
          prefix,
          "stats"
        ),
        "arn:aws:ssm:%s:%s:parameter/funds/*".formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount()
        ),
        "arn:aws:ssm:%s:%s:parameter/stats".formatted(
          Stack.of(this).getRegion(),
          Stack.of(this).getAccount()
        )
      ))
      .build();

    PolicyStatement writeStatusesPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:PutParameter"))
      .resources(
        StreamEx.of(
            "arn:aws:ssm:%s:%s:parameter/%s/%s",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/",
            "arn:aws:ssm:%s:%s:parameter/%s/%s/*"
          ).map(pattern -> pattern.formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount(),
            prefix,
            statuses
          ))
          .append("arn:aws:ssm:%s:%s:parameter/funds/*".formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount()
          ))
          .append("arn:aws:ssm:%s:%s:parameter/stats".formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount()
          ))
          .toList())
      .build();

    lambda.getRole().attachInlinePolicy(
      new Policy(this, id + "ParameterStorePolicy", PolicyProps.builder()
        .statements(List.of(readPolicy, readPolicy2, writeStatusesPolicy))
        .build())
    );
  }

  private void createRunSchedule(final Alias lambdaAlias) {
    final var fullUpdaterRule = Rule.Builder.create(this, "BankStatusFullUpdaterRule")
      .schedule(Schedule.rate(Duration.minutes(30)))
      .build();

    final var targetFull = LambdaFunction.Builder.create(lambdaAlias)
      .retryAttempts(0)
      .event(RuleTargetInput.fromObject(Map.of("check", "FULL")))
      .build();

    fullUpdaterRule.addTarget(targetFull);

    final var currentUpdaterRule = Rule.Builder.create(this, "BankStatusCurrentUpdaterRule")
      .schedule(Schedule.rate(Duration.minutes(10)))
      .build();

    final var targetCurrent = LambdaFunction.Builder.create(lambdaAlias)
      .retryAttempts(0)
      .event(RuleTargetInput.fromObject(Map.of("check", "CURRENT")))
      .build();

    currentUpdaterRule.addTarget(targetCurrent);

    final var statsUpdaterRule = Rule.Builder.create(this, "BankStatusStatsUpdaterRule")
      .schedule(Schedule.rate(Duration.hours(6)))
      .build();

    final var targetStats = LambdaFunction.Builder.create(lambdaAlias)
      .retryAttempts(0)
      .event(RuleTargetInput.fromObject(Map.of("check", "STATS")))
      .build();

    statsUpdaterRule.addTarget(targetStats);
  }

  private Alias createVersionAndUpdateAlias(final Function lambda, final String id) {
    final var lambdaCurrentVersion = lambda.getCurrentVersion();
    return Alias.Builder.create(this, id + "LambdaAlias")
      .aliasName("prod")
      .version(lambdaCurrentVersion)
      .build();
  }

  private void createBankStatusUpdaterLambda() {
    final var lambda = Function.Builder.create(this, "BankStatusUpdater")
      .runtime(Runtime.JAVA_21)
      .code(Code.fromAsset("../lambda-bank-status-updater/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_WEEK)
      .memorySize(512)
      .timeout(Duration.minutes(10))
      .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
      .build();

    addSsmReadPermissionsForBankStatusUpdaterLambda(lambda, "BankStatusUpdater");

    final Alias bankStatusUpdaterAlias = createVersionAndUpdateAlias(lambda, "BankStatusUpdater");

    createRunSchedule(bankStatusUpdaterAlias);
  }

  private Alias createTelegramBotLambda() {
    final var lambda = Function.Builder.create(this, "BankTelegramBot")
      .runtime(Runtime.JAVA_21)
//      .runtime(Runtime.PROVIDED_AL2023)
      .code(Code.fromAsset("../lambda-telegram-bot/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_WEEK)
      .memorySize(256)
//      .memorySize(128)
      .timeout(Duration.seconds(25))
      .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
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
        .statements(List.of(readSecretPolicy, writeAndReadStatusesPolicy))
        .build())
    );

    return createVersionAndUpdateAlias(lambda, "BankTelegramBot");
  }

  private Alias createBotAuthorizerLambda() {
    final var lambda = Function.Builder.create(this, "BotAuthorizer")
      .runtime(Runtime.PROVIDED_AL2023)
      .code(Code.fromAsset("../lambda-bot-authorizer/build/function.zip"))
      .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
      .logRetention(RetentionDays.ONE_WEEK)
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

  private Alias createStatusApiLambda() {
    final var lambda = Function.Builder.create(this, "StatusApiLambda")
      .runtime(Runtime.PROVIDED_AL2023)
      .code(Code.fromAsset("../lambda-status-api/build/function.zip"))
      .handler("not.used")
      .logRetention(RetentionDays.ONE_WEEK)
      .memorySize(128)
      .timeout(Duration.seconds(10))
      .environment(Map.of(
        "DISABLE_SIGNAL_HANDLERS", "true",
        "QUARKUS_HTTP_CORS_ORIGINS", CMW22_CORS_ORIGIN
      ))
      .build();

    PolicyStatement readPolicy = PolicyStatement.Builder.create()
      .actions(List.of("ssm:GetParameter"))
      .resources(
        Stream.of(
            "arn:aws:ssm:%s:%s:parameter/funds/*"
          ).map(pattern -> pattern.formatted(
            Stack.of(this).getRegion(),
            Stack.of(this).getAccount()
          ))
          .toList())
      .build();

    lambda.getRole().attachInlinePolicy(
      new Policy(this, "StatusApiLambda" + "ParameterStorePolicy", PolicyProps.builder()
        .statements(List.of(readPolicy))
        .build())
    );

    return createVersionAndUpdateAlias(lambda, "StatusApiLambda");
  }

  private void customDomainForRestApi(final RestApi restApi) {
    final Certificate cert = Certificate.Builder.create(this, "YuriyTkachComCertificate")
      .domainName("*.cmw22-bot.yuriytkach.com")
      .subjectAlternativeNames(List.of("cmw22-bot.yuriytkach.com"))
      .validation(CertificateValidation.fromDns())
      .certificateName("cmw22-bot.yuriytkach.com")
      .build();

    // Create a custom domain for the API Gateway
    final DomainNameProps domainOptions = DomainNameProps.builder()
      .certificate(cert)
      .domainName("cmw22-bot.yuriytkach.com")
      .build();

    final DomainName domainName = new DomainName(this, "APIDomain", domainOptions);

    // Map the custom domain to the API Gateway
    domainName.addBasePathMapping(restApi);
  }

}
