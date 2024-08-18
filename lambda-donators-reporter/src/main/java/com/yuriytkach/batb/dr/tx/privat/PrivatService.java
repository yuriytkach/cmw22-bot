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
import one.util.streamex.StreamEx;

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
    final LocalDate startDate,
    final LocalDate endDate
  ) {
    log.debug("Fetching privat TXes for account: {} ...", account);

    final var startDateStr = privatbankUtils.formatDate(startDate);
    final var endDateStr = privatbankUtils.formatDate(endDate);

    final var result = readTransactions(bankToken.value(), startDateStr, endDateStr, account, null)
      .filter(this::isCreditTransaction)
      .map(this::mapTx)
      .flatMap(Optional::stream)
      .collect(Collectors.toUnmodifiableSet());

    final long amount = StreamEx.of(result)
      .mapToLong(DonationTransaction::amountUah)
      .sum();

    log.info("Mapped privat TXes for account {}: {}", account, result.size());
    log.info("Total amount for account {}: {}", account, amount);
    if (log.isTraceEnabled()) {
      StreamEx.of(result).forEach(tx -> log.trace("Mapped TX: {}", tx));
    }

    return result;
  }

  private Stream<Transaction> readTransactions(
    final String token,
    final String startDate,
    final String endDate,
    final String account,
    final String followId
  ) {
    final var response = privatApi.transactions(token, startDate, endDate, account, followId, LIMIT);
    final Stream<Transaction> nextResponse;
    if (response == null || !"SUCCESS".equals(response.status())) {
      log.error("Failed to fetch privat TXes for account: {}. Response: {}", account, response);
      nextResponse = Stream.empty();
    } else if (response.existsNextPage()) {
      nextResponse = readTransactions(token, startDate, endDate, account, response.nextPageId());
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
    final var name = privatTxNameMapper.mapDonatorName(tx).orElse(DonationTransaction.UNKNOWN);

    if (log.isDebugEnabled() && DonationTransaction.SELF_NAME.equals(name)) {
      log.debug("Skipping self TX from fund: {}", tx);
      return Optional.empty();
    }

    if (log.isDebugEnabled() && DonationTransaction.UNKNOWN.equals(name)) {
      log.debug("Cannot map name from privat TX: {}", tx);
    }

    return Optional.of(DonationTransaction.builder()
      .id(tx.id())
      .amountUah(privatbankUtils.parseAmount(tx.sumEquivalent()))
      .date(privatbankUtils.safeParseDateTime(tx.dateTime()).orElse(null))
      .name(name)
      .build());
  }

}
