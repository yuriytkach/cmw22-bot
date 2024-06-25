package com.yuriytkach.batb.ba;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.yuriytkach.batb.common.secret.SecretsReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LambdaBotAuthorizer implements RequestHandler<AuthorizerRequest, PolicyResponse>  {

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;
  private final ResponseCreator responseCreator;

  @Override
  public PolicyResponse handleRequest(final AuthorizerRequest input, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Lambda Bot Authorizer. AWS Request ID: {}", context.getAwsRequestId());

    final var headerValue = input.headers().get(appProperties.telegramHeader());

    if (headerValue == null) {
      log.info("Telegram header not found: {}", appProperties.telegramHeader());
      return deny(input);
    }

    final var knownToken = secretsReader.readSecret(appProperties.secretName());

    if (knownToken.isEmpty()) {
      log.info("Secret not found: {}", appProperties.secretName());
      return deny(input);
    }

    if (headerValue.equals(knownToken.get())) {
      log.info("Token is valid");
      return allow(input);
    } else {
      log.info("Token is invalid");
      return deny(input);
    }
  }

  private PolicyResponse allow(final AuthorizerRequest input) {
    final var arn = input.methodArn().replaceFirst("prod/POST/.*", "prod/POST/*");
    return responseCreator.createResponse(IamPolicyResponse.ALLOW, arn);
  }

  private PolicyResponse deny(final AuthorizerRequest input) {
    return responseCreator.createResponse(IamPolicyResponse.DENY, input.methodArn());
  }
}
