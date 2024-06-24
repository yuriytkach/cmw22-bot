package com.yuriytkach.batb.common.ai;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

@Slf4j
@ApplicationScoped
public class LambdaInvoker {

  public String invokeLambda(final String functionName, final String payload) {
    try (var lambdaClient = LambdaClient.builder().build()) {
      final var invokeRequest = InvokeRequest.builder()
        .functionName(functionName)
        .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
        .build();

      log.debug("Invoking Lambda AI service: {}", functionName);
      final var invokeResponse = lambdaClient.invoke(invokeRequest);

      log.info("Received lambda response: {}", invokeResponse.statusCode());

      final String result = invokeResponse.payload().asString(StandardCharsets.UTF_8);
      log.debug("Lambda response payload: {}", result);

      return result;
    } catch (final Exception ex) {
      log.error("Failed to invoke Lambda AI service: {}", ex.getMessage());
      return StringUtils.EMPTY;
    }
  }
}
