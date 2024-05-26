package com.yuriytkach.batb.ai.config;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RegisterForReflection
public class OpenApiKeyAwsSsmConfigSourceFactory implements ConfigSourceFactory {

  static final String APP_AWS_CONFIG_KEY_OPENAPI = "app.aws-config-key.openapi";

  static final AtomicReference<String> configKeyName = new AtomicReference<>();
  static final AtomicReference<AwsConfigs> overriddenAwsConfigs = new AtomicReference<>();

  @Override
  public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context) {
    final ConfigValue value = context.getValue(APP_AWS_CONFIG_KEY_OPENAPI);
    if (value == null || value.getValue() == null) {
      log.info("AWS SSM Parameter Store key for OpenAI API key is not set: {}", APP_AWS_CONFIG_KEY_OPENAPI);
      return List.of();
    }
    log.info("AWS SSM Parameter Store key for OpenAI API key: {}", value.getValue());
    configKeyName.set(value.getValue());

    final var endpointOverride = context.getValue("quarkus.ssm.endpoint-override");
    if (endpointOverride != null && endpointOverride.getValue() != null) {
      log.info("AWS SSM endpoint override: {}", endpointOverride.getValue());

      overriddenAwsConfigs.set(new AwsConfigs(
        endpointOverride.getValue(),
        context.getValue("quarkus.ssm.aws.region").getValue(),
        context.getValue("quarkus.ssm.aws.credentials.static-provider.access-key-id").getValue(),
        context.getValue("quarkus.ssm.aws.credentials.static-provider.secret-access-key").getValue()
      ));
    }

    return List.of();
  }

  @Override
  public OptionalInt getPriority() {
    return OptionalInt.of(290);
  }

  @RegisterForReflection
  public record AwsConfigs(String endpoint, String region, String key, String secret) { }
}
