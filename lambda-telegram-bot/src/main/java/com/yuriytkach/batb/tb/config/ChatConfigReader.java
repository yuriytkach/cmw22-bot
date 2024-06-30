package com.yuriytkach.batb.tb.config;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.secret.SecretsReader;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ChatConfigReader {

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;

  public Optional<ChatConfiguration> readConfig() {
    return secretsReader.readSecret(appProperties.chatIdSecretKey())
      .map(secret -> {
        try {
          return objectMapper.readValue(secret, ChatConfiguration.class);
        } catch (final Exception ex) {
          log.error("Error while reading chat config: {}", ex.getMessage());
          return null;
        }
      });
  }
}
