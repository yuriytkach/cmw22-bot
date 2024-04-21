package com.yuriytkach.batb.dr.tx.privat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.common.BankToken;
import com.yuriytkach.batb.common.privatbank.PrivatbankUtils;
import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.privat.api.PrivatApi;
import com.yuriytkach.batb.dr.tx.privat.api.Transaction;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class PrivatService {

  public static final int LIMIT = 100;

  static final String TRANSACTION_TYPE_CREDIT = "C";
  static final String TRANSACTION_STATUS_PASSED = "r";

  @RestClient
  private final PrivatApi privatApi;

  private final PrivatbankUtils privatbankUtils;
  private final PrivatTxNameMapper privatTxNameMapper;

  Set<DonationTransaction> fetchTxesForAccount(
    final String account,
    final BankToken bankToken,
    final LocalDate startDate
  ) {
    log.debug("Fetching privat TXes for account: {} ...", account);

    final var startDateStr = privatbankUtils.formatDate(startDate);

    final var result = readTransactions(bankToken.value(), startDateStr, account, null)
      .filter(this::isCreditTransaction)
      .map(this::mapTx)
      .flatMap(Optional::stream)
      .collect(Collectors.toUnmodifiableSet());

    log.info("Mapped privat TXes for account {}: {}", account, result.size());

    return result;
  }

  private Stream<Transaction> readTransactions(
    final String token,
    final String startDate,
    final String account,
    final String followId
  ) {
    final var response = privatApi.transactions(token, startDate, account, followId, LIMIT);
    final Stream<Transaction> nextResponse;
    if (response == null || !"SUCCESS".equals(response.status())) {
      log.error("Failed to fetch privat TXes for account: {}. Response: {}", account, response);
      nextResponse = Stream.empty();
    } else if (response.existsNextPage()) {
      nextResponse = readTransactions(token, startDate, account, response.nextPageId());
    } else {
      nextResponse = Stream.empty();
    }
    log.debug("Fetched privat TXes for account {}: {}", account, response.transactions().size());

    return Stream.concat(response.transactions().stream(), nextResponse);
  }

  private boolean isCreditTransaction(final Transaction transaction) {
    return transaction.type().equals(TRANSACTION_TYPE_CREDIT)
      && transaction.status().equals(TRANSACTION_STATUS_PASSED);
  }

  private Optional<DonationTransaction> mapTx(final Transaction tx) {
    final var rez = privatTxNameMapper.mapDonatorName(tx).map(name -> DonationTransaction.builder()
      .id(tx.id())
      .amountUah(privatbankUtils.parseAmount(tx.sumEquivalent()))
      .date(privatbankUtils.safeParseDateTime(tx.dateTime()).orElse(null))
      .name(name)
      .build());

    if (log.isTraceEnabled() && rez.isEmpty()) {
      log.trace("Cannot map name from privat TX: {}", tx);
    }

    return rez;
  }

}
