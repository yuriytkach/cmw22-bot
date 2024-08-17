package com.yuriytkach.batb.dr.translation;

import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@ApplicationScoped
@IfBuildProfile("dev")
public class DevLambdaClientFactory {

  @Inject
  DevLambdaClientProperties properties;

  @Produces
  @ApplicationScoped
  LambdaClient createLambdaClient() {
    return LambdaClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
      ))
      .region(Region.of(properties.region()))
      .build();
  }

  @IfBuildProfile("dev")
  @ConfigMapping(prefix = "dev-lambda-client")
  public interface DevLambdaClientProperties {
    @WithDefault("accessKey")
    String accessKey();
    @WithDefault("secretKey")
    String secretKey();
    @WithDefault("eu-west-1")
    String region();
  }

}
