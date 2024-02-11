package com.yuriytkach.batb.bsu.config;

import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.BankAccessStorage;
import com.yuriytkach.batb.common.storage.FundInfoStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmAccountBalanceStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmBankAccessStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmFundInfoStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.ssm.SsmClient;

@ApplicationScoped
public class CommonBeans {

  @Produces
  @ApplicationScoped
  Clock clock() {
    return Clock.systemUTC();
  }

  @Produces
  @ApplicationScoped
  BankAccessStorage bankAccessStorage(
    final SsmClient ssmClient,
    final SsmProperties ssmProperties,
    final ObjectMapper objectMapper
  ) {
    return new SsmBankAccessStorage(ssmClient, ssmProperties, objectMapper);
  }

  @Produces
  @ApplicationScoped
  AccountBalanceStorage accountBalanceStorage(
    final SsmClient ssmClient,
    final SsmProperties ssmProperties,
    final ObjectMapper objectMapper
  ) {
    return new SsmAccountBalanceStorage(ssmClient, ssmProperties, objectMapper);
  }

  @Produces
  @ApplicationScoped
  FundInfoStorage fundInfoStorage(
    final SsmClient ssmClient,
    final SsmProperties ssmProperties,
    final ObjectMapper objectMapper
  ) {
    return new SsmFundInfoStorage(ssmClient, ssmProperties, objectMapper);
  }
}
