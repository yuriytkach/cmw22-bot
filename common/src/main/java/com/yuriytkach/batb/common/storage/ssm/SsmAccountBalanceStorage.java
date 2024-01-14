package com.yuriytkach.batb.common.storage.ssm;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
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

  private void save(final BankAccountStatus bankAccountStatus) {
    final String path = getPath(bankAccountStatus);
    log.info("Saving account balance to SSM path: {}", path);

    try {
      final var request = PutParameterRequest.builder()
        .name(path)
        .value(objectMapper.writeValueAsString(bankAccountStatus))
        .type(ParameterType.SECURE_STRING)
        .overwrite(true)
        .description("Account balance status")
        .build();

      final var response = ssmClient.putParameter(request);
      log.info("Account balance saved to SSM path `{}` with version: {}", path, response.version());
    } catch (final Exception ex) {
      log.error("Cannot save account balance to SSM path `{}`: {}", path, ex.getMessage());
    }
  }

  private String getPath(final BankAccountStatus bankAccountStatus) {
    return ssmProperties.prefix()
      + bankAccountStatus.bankType().name().toLowerCase()
      + ssmProperties.statuses() + "/"
      + bankAccountStatus.shortAccountId();
  }
}
