package com.yuriytkach.batb.common.storage.ssm;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankToken;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.json.JsonReader;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SsmBankAccessStorage implements BankAccessStorage {

  private final SsmClient ssmClient;
  private final SsmProperties ssmProperties;
  private final ObjectMapper objectMapper;
  private final JsonReader jsonReader;

  @Override
  @CacheResult(cacheName = "ssm-tokens")
  public List<BankToken> getListOfTokens(final BankType bankType) {
    final String path = ssmProperties.prefix() + bankType.name().toLowerCase() + ssmProperties.tokens();
    return readParamsByPath(path, "tokens");
  }

  @Override
  public List<BankAccount> getListOfAccounts(final BankType bankType) {
    final String path = ssmProperties.prefix() + bankType.name().toLowerCase() + ssmProperties.accounts();
    log.info("Getting accounts from SSM path: {}", path);

    final var request = GetParameterRequest.builder()
      .name(path)
      .build();

    try {
      final var response = ssmClient.getParameter(request);
      final var valueJson = response.parameter().value();
      return objectMapper.readerForListOf(BankAccount.class).readValue(valueJson);

    } catch (final Exception ex) {
      log.error("Cannot read ssm parameter by path `{}`: {}", path, ex.getMessage());
      return List.of();
    }
  }

  @Override
  @CacheResult(cacheName = "ssm-registry-config")
  public Optional<BankAccount> getRegistryConfig() {
    final String path = ssmProperties.prefix() + ssmProperties.registryGsheet();
    return readBankAccountObject(path);
  }

  @Override
  @CacheResult(cacheName = "ssm-stats-config")
  public Optional<BankAccount> getStatsConfig() {
    final String path = ssmProperties.prefix() + ssmProperties.statsGsheet();
    return readBankAccountObject(path);
  }

  @Override
  @CacheResult(cacheName = "ssm-donators-config")
  public Optional<BankAccount> getDonatorsConfig() {
    final String path = ssmProperties.prefix() + ssmProperties.donatorsGsheet();
    return readBankAccountObject(path);
  }

  private List<BankToken> readParamsByPath(final String path, final String paramTypeName) {
    log.info("Getting {} from SSM path: {}", paramTypeName, path);

    final var request = GetParametersByPathRequest.builder()
      .withDecryption(true)
      .path(path)
      .build();

    try {
      final GetParametersByPathResponse response = ssmClient.getParametersByPath(request);
      if (response.hasParameters()) {
        final var result = response.parameters().stream()
          .map(param -> new BankToken(param.name(), param.value()))
          .toList();
        log.info("Found {} by path `{}`: {}", paramTypeName, path, result.size());
        return result;
      } else {
        log.warn("No {} found by path `{}`", paramTypeName, path);
        return List.of();
      }
    } catch (final Exception ex) {
      log.error("Cannot read ssm parameters by path `{}`: {}", path, ex.getMessage());
      return List.of();
    }
  }

  private Optional<BankAccount> readBankAccountObject(final String path) {
    log.info("Getting bank account config from SSM path: {}", path);
    final var request = GetParameterRequest.builder()
      .name(path)
      .build();
    try {
      final var response = ssmClient.getParameter(request);
      final var valueJson = response.parameter().value();
      return jsonReader.readValue(valueJson, BankAccount.class);
    } catch (final Exception ex) {
      log.error("Cannot read ssm parameter by path `{}`: {}", path, ex.getMessage());
      return Optional.empty();
    }
  }
}
