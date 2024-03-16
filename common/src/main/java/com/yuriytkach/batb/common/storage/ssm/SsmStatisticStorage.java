package com.yuriytkach.batb.common.storage.ssm;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.Statistic;
import com.yuriytkach.batb.common.storage.StatisticStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@Slf4j
@RequiredArgsConstructor
public class SsmStatisticStorage implements StatisticStorage {

  private final SsmClient ssm;
  private final SsmProperties ssmProperties;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<Statistic> get() {
    final String path = ssmProperties.statistics();
    log.info("Reading stats info from SSM path: {}", path);
    final var request = GetParameterRequest.builder()
      .name(path)
      .build();
    try {
      final GetParameterResponse response = ssm.getParameter(request);
      return Optional.ofNullable(response.parameter().value())
        .flatMap(this::deserializeStatistic);
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", path);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading param {}: {}", path, ex.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void save(final Statistic statistic) {
    log.info("Saving stats: {}", statistic);
    final String path = ssmProperties.statistics();
    serializeStatistic(statistic)
      .map(json -> PutParameterRequest.builder()
        .name(path)
        .value(json)
        .type(ParameterType.STRING)
        .overwrite(true)
        .description("Statistics information")
        .build())
      .ifPresent(request -> {
        try {
          final var response = ssm.putParameter(request);
          log.info("Stats saved to SSM path `{}` with version: {}", path, response.version());
        } catch (final Exception ex) {
          log.error("Cannot save stats to SSM path `{}`: {}", path, ex.getMessage());
        }
      });
  }

  private Optional<String> serializeStatistic(final Statistic stats) {
    try {
      return Optional.of(objectMapper.writeValueAsString(stats));
    } catch (final Exception ex) {
      log.error("Cannot serialize stats info: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  private Optional<Statistic> deserializeStatistic(final String json) {
    try {
      return Optional.of(objectMapper.readValue(json, Statistic.class));
    } catch (final Exception ex) {
      log.error("Cannot deserialize stats: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}
