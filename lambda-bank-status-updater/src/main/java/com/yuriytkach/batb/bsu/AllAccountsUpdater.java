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
