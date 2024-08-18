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

package com.yuriytkach.batb.dr.tx.mono.token;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yuriytkach.batb.common.json.JsonReader;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.DonationTransactionFetcher;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MonoTokenTxFetcher implements DonationTransactionFetcher {

  private final SecretsReader secretsReader;
  private final MonoTokenTxFetcherProperties props;
  private final JsonReader jsonReader;
  private final MonoTokenService service;
  private final MonoTokenPeriodDatesAdjuster periodDatesAdjuster;

  @Override
  public Set<DonationTransaction> fetchTransactions(final LocalDate startDate, final LocalDate endDate) {
    final Optional<String> configOpt = secretsReader.readSecret(props.configKey());

    if (configOpt.isEmpty()) {
      log.error("Cannot read mono-token TXes config from SSM");
      return Set.of();
    }

    final Optional<MonoTokenTxFetcherConfig> parsedConfigOpt = configOpt
      .flatMap(value -> jsonReader.readValue(value, MonoTokenTxFetcherConfig.class));

    if (parsedConfigOpt.isEmpty()) {
      log.error("Cannot parse mono-token TXes config");
      return Set.of();
    }

    return parsedConfigOpt.stream()
      .flatMap(config -> config.configs().stream())
      .flatMap(config -> readTxes(config, startDate, endDate))
      .collect(Collectors.toUnmodifiableSet());
  }

  private Stream<DonationTransaction> readTxes(
    final ConfigEntry config,
    final LocalDate startDate,
    final LocalDate endDate
  ) {
    return service.readClientInfo(config.token())
      .map(client -> {
        log.info(
          "Reading mono-token TXes from client {} ({}) for jars: {}",
          client.name(),
          client.clientId(),
          config.jars().size()
        );

        return periodDatesAdjuster.adjust(startDate, endDate).stream()
          .flatMap(period -> config.jars().stream()
            .flatMap(jar -> service.readJarTxes(config.token(), jar, period.startDate(), period.endDate()).stream()));
      })
      .orElseGet(() -> {
        log.error("Cannot read mono client info");
        return Stream.empty();
      });
  }

  @RegisterForReflection
  record MonoTokenTxFetcherConfig(List<ConfigEntry> configs) { }

  @RegisterForReflection
  record ConfigEntry(String token, List<String> jars) { }
}
