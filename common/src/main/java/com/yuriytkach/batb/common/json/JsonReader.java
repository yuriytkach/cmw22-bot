package com.yuriytkach.batb.common.json;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class JsonReader {

  private final ObjectMapper objectMapper;

  public <T> Optional<T> readValue(final String value, final Class<T> clazz) {
    try {
      return Optional.ofNullable(objectMapper.readValue(value, clazz));
    } catch (final JsonProcessingException ex) {
      log.error("Failed to parse json value: {}", ex.getMessage());
      return Optional.empty();
    }
  }

}
