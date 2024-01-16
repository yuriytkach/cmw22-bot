package com.yuriytkach.batb.tb.config;

import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.secret.ssm.SsmSecretsReader;

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

}
