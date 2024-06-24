package com.yuriytkach.batb.dr.tx.mono.token;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.mono.token.api.Client;
import com.yuriytkach.batb.dr.tx.mono.token.api.MonoApi;
import com.yuriytkach.batb.dr.tx.mono.token.api.MonoStatement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
class MonoTokenService {

  @Inject
  @RestClient
  MonoApi monoApi;

  @Inject
  Clock clock;

  @Inject
  MonoTxNameMapper monoTxNameMapper;

  Optional<Client> readClientInfo(final String token) {
    log.info("Reading client info...");
    try {
      return Optional.of(monoApi.clientInfo(token));
    } catch (final Exception ex) {
      log.error("Cannot read mono client info: {}", ex.getMessage());
      return Optional.empty();
    }
  }

  public Set<DonationTransaction> readJarTxes(
    final String token,
    final String jar,
    final LocalDate startDate,
    final LocalDate endDate
  ) {
    log.debug("Fetching mono-token TXes for jar: {} for dates: {} - {}", jar, startDate, endDate);
    final var result = readTransactions(token, jar, toEpochMilli(startDate), toEpochMilli(endDate.plusDays(1)) - 1)
      .filter(this::isCreditTransaction)
      .map(this::mapTx)
      .collect(Collectors.toUnmodifiableSet());

    final long totalAmount = StreamEx.of(result)
      .mapToLong(DonationTransaction::amountUah)
      .sum();

    log.info("Mapped mono-token TXes for jar {}: {}", jar, result.size());
    log.info("Total amount for jar {}: {}", jar, totalAmount);
    if (log.isTraceEnabled()) {
      StreamEx.of(result).forEach(tx -> log.trace("Mapped TX: {}", tx));
    }

    return result;
  }

  private DonationTransaction mapTx(final MonoStatement tx) {
    final var name = monoTxNameMapper.mapDonatorName(tx.description()).orElse(DonationTransaction.UNKNOWN);

    if (log.isDebugEnabled() && DonationTransaction.UNKNOWN.equals(name)) {
      log.debug("Cannot map name from mono-token TX: {}", tx);
    }

    return DonationTransaction.builder()
      .id(tx.id())
      .amountUah(tx.operationAmount())
      .date(Instant.ofEpochSecond(tx.time()))
      .name(name)
      .build();
  }

  private boolean isCreditTransaction(final MonoStatement statement) {
    return !statement.hold() && statement.operationAmount() > 0;
  }

  /**
   * Повертає 500 транзакцій з кінця, тобто від часу to до from.
   * Якщо кількість транзакцій = 500, потрібно зробити ще один запит,
   * зменшивши час to до часу останнього платежу, з відповіді.
   * Якщо знову кількість транзакцій = 500, то виконуєте запити до того часу,
   * поки кількість транзакцій не буде < 500. Відповідно, якщо кількість транзакцій < 500,
   * то вже отримано всі платежі за вказаний період.
   */
  private Stream<MonoStatement> readTransactions(
    final String token,
    final String jar,
    final long startTimestamp,
    final long endTimestamp
  ) {
    final List<MonoStatement> statements = monoApi.statements(token, jar, startTimestamp, endTimestamp);
    final Stream<MonoStatement> nextResponse;
    if (statements.size() > 500) {
      return readTransactions(token, jar, startTimestamp, statements.getLast().time() - 1);
    } else {
      nextResponse = Stream.empty();
    }
    log.debug("Fetched mono-token TXes for jar {}: {}", jar, statements.size());
    return Stream.concat(statements.stream(), nextResponse);
  }

  private long toEpochMilli(final LocalDate from) {
    return from.atStartOfDay(clock.getZone()).toInstant().toEpochMilli();
  }
}
