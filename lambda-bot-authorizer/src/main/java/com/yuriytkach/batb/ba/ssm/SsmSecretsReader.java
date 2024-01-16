package com.yuriytkach.batb.ba.ssm;

import java.util.Optional;

import com.yuriytkach.batb.ba.SecretsReader;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SsmSecretsReader implements SecretsReader {

  private final SsmClient ssm;

  @Override
  public Optional<String> readSecret(final String secretName) {
    final var request = GetParameterRequest.builder()
      .name(secretName)
      .withDecryption(true)
      .build();
    try {
      final GetParameterResponse response = ssm.getParameter(request);
      return Optional.of(response.parameter().value());
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", secretName);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading secret {}: {}", secretName, ex.getMessage());
      return Optional.empty();
    }
  }
}
