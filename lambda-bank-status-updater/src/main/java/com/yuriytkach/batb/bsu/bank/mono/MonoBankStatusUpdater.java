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

package com.yuriytkach.batb.bsu.bank.mono;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.MDC;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.bsu.bank.mono.api.MonoJar;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.Currency;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MonoBankStatusUpdater implements BankStatusUpdater {

  private static final BankType BANK_TYPE = BankType.MONO;

  private final BankAccessStorage bankAccessStorage;
  private final AccountBalanceStorage accountBalanceStorage;
  private final MonoService monoService;
  private final Clock clock;

  @Override
  public BankType bankType() {
    return BANK_TYPE;
  }

  @Override
  public Set<BankAccountStatus> updateAllAccountStatuses(final Map<String, BankAccountStatus> previousStatuses) {
    MDC.put("bankType", BANK_TYPE.name());

    final Set<BankAccount> configuredAccounts = readBankAccounts();
    log.info("Updating all account statuses for Monobank: {}", configuredAccounts.size());

    return updateAccounts(previousStatuses, configuredAccounts, true);
  }

  @Override
  public Set<BankAccountStatus> updateSpecifiedAccountStatuses(final Map<String, BankAccountStatus> specificStatuses) {
    final Set<BankAccount> accountsToUpdate = StreamEx.of(specificStatuses.values())
      .map(status -> new BankAccount(status.accountId(), status.accountName(), Map.of(), status.gsheetStatRow()))
      .toImmutableSet();

    log.info("Updating account statuses for Monobank: {}", accountsToUpdate);
    return updateAccounts(specificStatuses, accountsToUpdate, false);
  }

  private Set<BankAccountStatus> updateAccounts(
    final Map<String, BankAccountStatus> previousStatuses,
    final Set<BankAccount> accounts,
    final boolean includeJarOwnerInName
  ) {
    final var retrievedAccountStatuses = StreamEx.of(accounts)
      .mapToEntry(account -> monoService.readJarStatus(account.id()))
      .flatMapValues(Optional::stream)
      .mapKeyValue((bankAccount, monoJar) -> buildBankAccountStatus(bankAccount, monoJar, includeJarOwnerInName))
      .flatMap(Optional::stream)
      .toImmutableSet();
    log.info("Total bank account statuses retrieved: {}", retrievedAccountStatuses.size());

    final var updatedAccountStatuses = findUpdatedStatuses(previousStatuses, retrievedAccountStatuses);
    log.info("Total bank account statuses updated: {}", updatedAccountStatuses.size());

    accountBalanceStorage.saveAll(updatedAccountStatuses);

    return previousStatusesWithUpdated(previousStatuses, updatedAccountStatuses);
  }

  private Set<BankAccount> readBankAccounts() {
    return StreamEx.of(bankAccessStorage.getListOfAccounts(BANK_TYPE))
      .distinct(BankAccount::id)
      .toImmutableSet();
  }

  private Optional<BankAccountStatus> buildBankAccountStatus(
    final BankAccount bankAccount,
    final MonoJar monoJar,
    final boolean includeJarOwnerInName
  ) {
    final Optional<Currency> curr = Currency.fromIsoCode(monoJar.currency());
    if (curr.isEmpty()) {
      log.warn("Unknown currency: {}. Cannot map account: {}", monoJar.currency(), bankAccount);
    }
    return curr.map(currency -> BankAccountStatus.builder()
      .accountId(bankAccount.id())
      .accountName(
        includeJarOwnerInName ? "%s (%s)".formatted(bankAccount.name(), monoJar.ownerName()) : bankAccount.name()
      )
      .bankType(BANK_TYPE)
      .amount(monoJar.amount())
      .amountUah(monoJar.amount())
      .currency(currency)
      .updatedAt(clock.instant())
      .gsheetStatRow(bankAccount.gsheetStatRow())
      .build()
    );
  }
}
