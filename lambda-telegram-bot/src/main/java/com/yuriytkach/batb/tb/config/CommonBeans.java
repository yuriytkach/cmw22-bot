package com.yuriytkach.batb.tb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.secret.ssm.SsmSecretsReader;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmAccountBalanceStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.ssm.SsmClient;

@ApplicationScoped
public class CommonBeans {

  @Produces
  @ApplicationScoped
  SecretsReader secretsReader(final SsmClient ssmClient) {
    return new SsmSecretsReader(ssmClient);
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
}
