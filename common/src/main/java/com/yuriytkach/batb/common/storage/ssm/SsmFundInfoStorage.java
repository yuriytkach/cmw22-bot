/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.yuriytkach.batb.common.storage.ssm;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SsmFundInfoStorage implements FundInfoStorage {

  private final SsmClient ssm;
  private final SsmProperties ssmProperties;
  private final ObjectMapper objectMapper;

  @Override
  public Optional<FundInfo> getCurrent() {
    final String path = ssmProperties.funds() + ssmProperties.currentFundProp();
    log.info("Reading current fund name from SSM path: {}", path);
    final var request = GetParameterRequest.builder()
      .name(path)
      .build();
    try {
      final GetParameterResponse response = ssm.getParameter(request);
      return Optional.ofNullable(response.parameter().value())
        .flatMap(this::getById);
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", path);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading param {}: {}", path, ex.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<FundInfo> getById(final String fundraiserId) {
    final String path = ssmProperties.funds() + fundraiserId;
    log.info("Reading fund info from SSM path: {}", path);
    final var request = GetParameterRequest.builder()
      .name(path)
      .build();
    try {
      final GetParameterResponse response = ssm.getParameter(request);
      return Optional.ofNullable(response.parameter().value())
        .flatMap(this::deserializeFundInfo);
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", path);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading param {}: {}", path, ex.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void save(final FundInfo fundInfo) {
    log.info("Saving fund info: {}", fundInfo);
    final String path = ssmProperties.funds() + fundInfo.id();
    serializeFundInfo(fundInfo)
      .map(json -> PutParameterRequest.builder()
        .name(path)
        .value(json)
        .type(ParameterType.STRING)
        .overwrite(true)
        .description("Fund information")
        .build())
      .ifPresent(request -> {
        try {
          final var response = ssm.putParameter(request);
          log.info("Fund info saved to SSM path `{}` with version: {}", path, response.version());
        } catch (final Exception ex) {
          log.error("Cannot save fund info to SSM path `{}`: {}", path, ex.getMessage());
        }
      });
  }

  private Optional<String> serializeFundInfo(final FundInfo fundInfo) {
    try {
      return Optional.of(objectMapper.writeValueAsString(fundInfo));
    } catch (final Exception ex) {
      log.error("Cannot serialize fund info: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  private Optional<FundInfo> deserializeFundInfo(final String json) {
    try {
      return Optional.of(objectMapper.readValue(json, FundInfo.class));
    } catch (final Exception ex) {
      log.error("Cannot deserialize fund info: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}
