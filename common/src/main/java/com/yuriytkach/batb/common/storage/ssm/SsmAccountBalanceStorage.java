package com.yuriytkach.batb.common.storage.ssm;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@Slf4j
@RequiredArgsConstructor
public class SsmAccountBalanceStorage implements AccountBalanceStorage {

  private final SsmClient ssmClient;
  private final SsmProperties ssmProperties;
  private final ObjectMapper objectMapper;

  @Override
  public void saveAll(final Set<BankAccountStatus> bankAccountStatuses) {
    bankAccountStatuses.forEach(this::save);
  }

  @Override
  public Set<BankAccountStatus> getAll() {
    final String path = ssmProperties.prefix() + ssmProperties.statuses();

    try {
      final Set<BankAccountStatus> finalRez = new HashSet<>();
      String nextToken = null;
      Set<BankAccountStatus> result;
      do {
        final var request = GetParametersByPathRequest.builder()
          .path(path);

        if (nextToken != null) {
          request.nextToken(nextToken);
        }
        log.info("Reading ssm parameters by path `{}`. Next token: {}", path, nextToken != null);

        final GetParametersByPathResponse response = ssmClient.getParametersByPath(request.build());
        if (response.hasParameters()) {
          result = StreamEx.of(response.parameters())
            .map(Parameter::value)
            .map(this::deserializeBankStatus)
            .flatMap(Optional::stream)
            .toImmutableSet();
          log.info("Found account balances by path `{}`: {}", path, result.size());
          finalRez.addAll(result);
          nextToken = response.nextToken();
        } else {
          log.warn("No more params found by path `{}`", path);
          break;
        }
      } while (!result.isEmpty() && nextToken != null);
      log.info("Total account balances found by path `{}`: {}", path, finalRez.size());
      return Set.copyOf(finalRez);
    } catch (final Exception ex) {
      log.error("Cannot read ssm parameters by path `{}`: {}", path, ex.getMessage());
      return Set.of();
    }
  }

  @Override
  public Optional<BankAccountStatus> getById(final String accountId) {
    final String path = getPath(accountId);

    final var request = GetParameterRequest.builder()
      .name(path)
      .build();
    try {
      final GetParameterResponse response = ssmClient.getParameter(request);
      return Optional.of(response.parameter().value())
        .flatMap(this::deserializeBankStatus);
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", path);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading secret {}: {}", path, ex.getMessage());
      return Optional.empty();
    }
  }

  private Optional<BankAccountStatus> deserializeBankStatus(final String json) {
    try {
      return Optional.of(objectMapper.readValue(json, BankAccountStatus.class));
    } catch (final Exception ex) {
      log.error("Cannot deserialize bank account status: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  private void save(final BankAccountStatus bankAccountStatus) {
    final String path = getPath(bankAccountStatus.shortAccountId());
    log.info("Saving account balance to SSM path: {}", path);

    try {
      final var request = PutParameterRequest.builder()
        .name(path)
        .value(objectMapper.writeValueAsString(bankAccountStatus))
        .type(ParameterType.STRING)
        .overwrite(true)
        .description("Account balance status")
        .build();

      final var response = ssmClient.putParameter(request);
      log.info("Account balance saved to SSM path `{}` with version: {}", path, response.version());
    } catch (final Exception ex) {
      log.error("Cannot save account balance to SSM path `{}`: {}", path, ex.getMessage());
    }
  }

  private String getPath(final String accountId) {
    return ssmProperties.prefix() + ssmProperties.statuses() + "/" + accountId;
  }
}
