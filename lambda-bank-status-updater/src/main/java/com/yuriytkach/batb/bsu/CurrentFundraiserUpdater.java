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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class CurrentFundraiserUpdater {

  @Inject
  FundInfoStorage fundInfoStorage;

  @Inject
  AccountBalanceStorage accountBalanceStorage;

  @Inject
  Instance<BankStatusUpdater> updaters;

  @Inject
  FundService fundService;

  public void update() {
    fundInfoStorage.getCurrent()
      .ifPresentOrElse(
        this::updateFundData,
        () -> log.info("No current fundraiser found")
      );
  }

  private void updateFundData(final FundInfo fundInfo) {
    final var allAccountsByShortId = StreamEx.of(accountBalanceStorage.getAll())
      .mapToEntry(BankAccountStatus::shortAccountId, Function.identity())
      .toImmutableMap();

    final Map<BankType, List<BankAccountStatus>> accountsToCheck = StreamEx.of(fundInfo.accounts())
      .map(allAccountsByShortId::get)
      .filter(Objects::nonNull)
      .groupingBy(BankAccountStatus::bankType);

    final List<BankAccountStatus> fundStatuses = updaters.stream()
      .flatMap(updater -> updateAccountsForBankType(updater, accountsToCheck))
      .toList();

    fundService.updateCurrentFundraiser(fundInfo, fundStatuses);
  }

  private Stream<BankAccountStatus> updateAccountsForBankType(
    final BankStatusUpdater updater,
    final Map<BankType, List<BankAccountStatus>> accountsToCheck
  ) {
    final List<BankAccountStatus> accountsToUpdate = accountsToCheck.getOrDefault(updater.bankType(), List.of());
    if (accountsToUpdate.isEmpty()) {
      log.info("No accounts to update for bank type: {}", updater.bankType());
      return Stream.empty();
    } else {
      return updater.updateSpecifiedAccountStatuses(
        StreamEx.of(accountsToUpdate).mapToEntry(BankAccountStatus::accountId, Function.identity()).toImmutableMap()
      ).stream();
    }
  }
}
