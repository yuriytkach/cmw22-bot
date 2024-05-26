package com.yuriytkach.batb.ai.config;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@Slf4j
@RegisterForReflection
public class AwsSsmConfigSource implements ConfigSource {

  static final String CONFIG_KEY = "quarkus.langchain4j.openai.api-key";

  private final AtomicReference<String> configKeyValue = new AtomicReference<>();

  @Override
  public Set<String> getPropertyNames() {
    return Set.of(CONFIG_KEY);
  }

  @Override
  public int getOrdinal() {
    return 275;
  }

  @Override
  public String getValue(final String key) {
    if (CONFIG_KEY.equals(key)) {
      if (configKeyValue.get() == null) {
        log.info("Getting value for key: {}", key);
        final var awsSsmConfigKey = OpenApiKeyAwsSsmConfigSourceFactory.configKeyName.get();
        final var value = readSsmConfigKey(awsSsmConfigKey);
        if (value != null) {
          configKeyValue.set(value);
        }
        return value;
      } else {
        return configKeyValue.get();
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return AwsSsmConfigSource.class.getSimpleName();
  }

  private String readSsmConfigKey(final String keyName) {
    log.info("Reading SSM parameter store key: {}", keyName);
    try (SsmClient ssmClient = buildSsmClient()) {
      final var request = GetParameterRequest.builder()
        .name(keyName)
        .withDecryption(true)
        .build();
      final GetParameterResponse response = ssmClient.getParameter(request);
      log.debug("SSM parameter store key read successfully: {}", keyName);
      return response.parameter().value();
    } catch (final Exception ex) {
      log.warn("Failed to read SSM parameter store key: {}: {}", keyName, ex.getMessage());
      return null;
    }
  }

  private SsmClient buildSsmClient() {
    final var awsconfigs = OpenApiKeyAwsSsmConfigSourceFactory.overriddenAwsConfigs.get();
    if (awsconfigs == null) {
      return SsmClient.builder().build();
    } else {
      return SsmClient.builder()
        .region(Region.of(awsconfigs.region()))
        .credentialsProvider(
          StaticCredentialsProvider.create(AwsBasicCredentials.create(awsconfigs.key(), awsconfigs.secret())))
        .endpointOverride(URI.create(awsconfigs.endpoint()))
        .build();
    }
  }
}
