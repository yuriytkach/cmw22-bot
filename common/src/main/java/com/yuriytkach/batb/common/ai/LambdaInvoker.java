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

package com.yuriytkach.batb.common.ai;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class LambdaInvoker {

  private final ObjectMapper objectMapper;
  private final LambdaClient lambdaClient;

  public Optional<String> invokeAiLambda(
    final String functionName,
    final AiServiceType serviceType,
    final String payload
  ) {
    return createLambdaPayload(serviceType, payload)
      .map(lambdaPayload -> invokeLambda(functionName, lambdaPayload));
  }

  public String invokeLambda(final String functionName, final String payload) {
    try {
      final var invokeRequest = InvokeRequest.builder()
        .functionName(functionName)
        .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
        .build();

      log.debug("Invoking Lambda: {}", functionName);
      final var invokeResponse = lambdaClient.invoke(invokeRequest);

      log.info("Received lambda response: {}", invokeResponse.statusCode());

      final String result = invokeResponse.payload().asString(StandardCharsets.UTF_8);
      log.debug("Lambda response payload: {}", result);

      return result;
    } catch (final Exception ex) {
      log.error("Failed to invoke Lambda: {}", ex.getMessage());
      return null;
    }
  }

  private Optional<String> createLambdaPayload(final AiServiceType aiServiceType, final String payload) {
    try {
      return Optional.of(objectMapper.writeValueAsString(
        new LambdaAiServiceRequest(aiServiceType, payload)
      ));
    } catch (final Exception ex) {
      log.error("Failed to serialize Lambda payload: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}
