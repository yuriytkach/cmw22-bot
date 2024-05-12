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
        return config.jars.stream()
          .flatMap(jar -> service.readJarTxes(config.token(), jar, startDate, endDate).stream());
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
