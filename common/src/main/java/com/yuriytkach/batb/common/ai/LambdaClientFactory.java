package com.yuriytkach.batb.common.ai;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.lambda.LambdaClient;

@ApplicationScoped
public class LambdaClientFactory {

  @Produces
  @DefaultBean
  @ApplicationScoped
  LambdaClient createDefaultLambdaClient() {
    return LambdaClient.builder().build();
  }

}
