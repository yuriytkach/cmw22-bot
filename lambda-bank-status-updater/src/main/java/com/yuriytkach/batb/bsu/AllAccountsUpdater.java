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

package com.yuriytkach.batb.bsu;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.bsu.bank.gsheet.GSheetAllAccountsSyncService;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class AllAccountsUpdater {

  @Inject
  Instance<BankStatusUpdater> updaters;

  @Inject
  AccountBalanceStorage accountBalanceStorage;

  @Inject
  GSheetAllAccountsSyncService gSheetAllAccountsSyncService;

  public void update() {
    final var accountStatuses = accountBalanceStorage.getAll();
    log.debug("Loaded account statuses: {}", accountStatuses.size());

    log.info("Updating bank statuses with updaters: {}", updaters.stream().count());

    final var allAccountBalances = updaters.stream()
      .flatMap(updater -> updateStatusesForUpdater(updater, accountStatuses))
      .toList();

    gSheetAllAccountsSyncService.updateAllAccountsTotals(allAccountBalances);
  }

  private Stream<BankAccountStatus> updateStatusesForUpdater(
    final BankStatusUpdater updater,
    final Set<BankAccountStatus> accountStatuses
  ) {
    final var prevAccountStatuses = StreamEx.of(accountStatuses)
      .filter(acc -> acc.bankType() == updater.bankType())
      .mapToEntry(BankAccountStatus::accountId, Function.identity())
      .toImmutableMap();
    return updater.updateAllAccountStatuses(prevAccountStatuses).stream();
  }
}
