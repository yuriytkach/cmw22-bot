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

package com.yuriytkach.batb.common.secret.ssm;

import java.util.Optional;

import com.yuriytkach.batb.common.secret.SecretsReader;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SsmSecretsReader implements SecretsReader {

  private final SsmClient ssm;

  @Override
  @CacheResult(cacheName = "ssm-secrets")
  public Optional<String> readSecret(final String secretName) {
    log.debug("Reading secret: {}", secretName);
    final var request = GetParameterRequest.builder()
      .name(secretName)
      .withDecryption(true)
      .build();
    try {
      final GetParameterResponse response = ssm.getParameter(request);
      return Optional.of(response.parameter().value());
    } catch (final ParameterNotFoundException ex) {
      log.info("SSM Parameter not found: {}", secretName);
      return Optional.empty();
    } catch (final Exception ex) {
      log.error("Error reading secret {}: {}", secretName, ex.getMessage());
      return Optional.empty();
    }
  }
}
