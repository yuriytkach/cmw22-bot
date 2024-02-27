package com.yuriytkach.batb.status.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.storage.FundInfoStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmFundInfoStorage;
import com.yuriytkach.batb.common.storage.ssm.SsmProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.ssm.SsmClient;

@ApplicationScoped
public class CommonBeans {

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
