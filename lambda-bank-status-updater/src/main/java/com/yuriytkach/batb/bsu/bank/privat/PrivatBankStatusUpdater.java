package com.yuriytkach.batb.bsu.bank.privat;

import java.time.Clock;
import java.util.List;

import org.slf4j.MDC;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.bsu.bank.privat.api.Balance;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PrivatBankStatusUpdater implements BankStatusUpdater {

  private final BankAccessStorage bankAccessStorage;
  private final AccountBalanceStorage accountBalanceStorage;
  private final PrivatService privatService;
  private final PrivatBalanceAmountParser privatBalanceAmountParser;
  private final Clock clock;

  @Override
  public void updateAccountStatuses() {
    MDC.put("bankType", BankType.PRIVAT.name());
    log.info("Updating account statuses for PrivatBank");

    final List<String> accounts = bankAccessStorage.getListOfAccounts(BankType.PRIVAT);

    final List<String> tokens = bankAccessStorage.getListOfTokens(BankType.PRIVAT);
    log.info("Loaded tokens: {}", tokens.size());

    final var allBankAccountStatuses = StreamEx.of(tokens)
      .flatMap(token -> privatService.readAllAvailablePrivatAccounts(token).stream())
      .filter(balance -> shouldProcessAccount(balance, accounts))
      .map(this::buildBankAccountStatus)
      .toImmutableSet();
    log.info("Total bank account statuses: {}", allBankAccountStatuses.size());

    accountBalanceStorage.saveAll(allBankAccountStatuses);
  }

  private BankAccountStatus buildBankAccountStatus(final Balance balance) {
    return BankAccountStatus.builder()
      .accountId(balance.acc())
      .bankType(BankType.PRIVAT)
      .amount(privatBalanceAmountParser.parseAmount(balance.balanceOut()))
      .currency(balance.currency())
      .updatedAt(clock.instant())
      .build();
  }

  private boolean shouldProcessAccount(final Balance balance, final List<String> accounts) {
    final boolean shouldProcess = accounts.contains(balance.acc());
    if (!shouldProcess) {
      log.debug("Ignoring account: {}", balance.acc());
    }
    return shouldProcess;
  }
}
