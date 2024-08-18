/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
