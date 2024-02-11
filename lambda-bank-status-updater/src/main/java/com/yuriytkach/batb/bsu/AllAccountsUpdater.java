package com.yuriytkach.batb.bsu;

import java.util.function.Function;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
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

  public void update() {
    final var accountStatuses = StreamEx.of(accountBalanceStorage.getAll())
      .mapToEntry(BankAccountStatus::accountId, Function.identity())
      .toImmutableMap();
    log.debug("Loaded account statuses: {}", accountStatuses.size());

    log.info("Updating bank statuses with updaters: {}", updaters.stream().count());

    updaters.stream().forEach(updated -> updated.updateAllAccountStatuses(accountStatuses));
  }
}
