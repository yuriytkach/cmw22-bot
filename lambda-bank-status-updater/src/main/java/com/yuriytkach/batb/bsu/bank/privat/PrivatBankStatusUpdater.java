package com.yuriytkach.batb.bsu.bank.privat;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.MDC;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.bsu.bank.privat.api.Balance;
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
public class PrivatBankStatusUpdater implements BankStatusUpdater {

  private static final BankType BANK_TYPE = BankType.PRIVAT;

  private final BankAccessStorage bankAccessStorage;
  private final AccountBalanceStorage accountBalanceStorage;
  private final PrivatService privatService;
  private final PrivatBalanceAmountParser privatBalanceAmountParser;
  private final Clock clock;

  @Override
  public BankType bankType() {
    return BANK_TYPE;
  }

  @Override
  public void updateAllAccountStatuses(final Map<String, BankAccountStatus> previousStatuses) {
    MDC.put("bankType", BANK_TYPE.name());
    final Map<String, BankAccount> configuredAccounts = readBankAccounts();
    log.info("Updating all account statuses for PrivatBank: {}", configuredAccounts.size());

    updateAccountStatuses(previousStatuses, configuredAccounts);
  }

  @Override
  public Set<BankAccountStatus> updateSpecifiedAccountStatuses(final Map<String, BankAccountStatus> specificStatuses) {
    final Map<String, BankAccount> accountsToUpdate = StreamEx.of(specificStatuses.values())
      .map(status -> new BankAccount(status.accountId(), status.accountName(), Map.of()))
      .toMap(BankAccount::id, Function.identity());

    log.info("Updating account statuses for PrivatBank: {}", accountsToUpdate.keySet());
    return updateAccountStatuses(specificStatuses, accountsToUpdate);
  }

  private Set<BankAccountStatus> updateAccountStatuses(
    final Map<String, BankAccountStatus> previousStatuses,
    final Map<String, BankAccount> configuredAccounts
  ) {
    final List<String> tokens = bankAccessStorage.getListOfTokens(BANK_TYPE);
    log.info("Loaded tokens: {}", tokens.size());

    final var retrievedAccountStatuses = StreamEx.of(tokens)
      .flatMap(token -> privatService.readAllAvailablePrivatAccounts(token).stream())
      .filter(balance -> shouldProcessAccount(balance, configuredAccounts.keySet()))
      .map(balance -> buildBankAccountStatus(balance, configuredAccounts.get(balance.acc())))
      .flatMap(Optional::stream)
      .toImmutableSet();
    log.info("Total bank account statuses retrieved: {}", retrievedAccountStatuses.size());

    final var updatedAccountStatuses = findUpdatedStatuses(previousStatuses, retrievedAccountStatuses);
    log.info("Total bank account statuses updated: {}", updatedAccountStatuses.size());

    accountBalanceStorage.saveAll(updatedAccountStatuses);

    return previousStatusesWithUpdated(previousStatuses, updatedAccountStatuses);
  }

  private Map<String, BankAccount> readBankAccounts() {
    return StreamEx.of(bankAccessStorage.getListOfAccounts(BANK_TYPE))
      .mapToEntry(BankAccount::id, Function.identity())
      .distinctKeys()
      .toImmutableMap();
  }

  private Optional<BankAccountStatus> buildBankAccountStatus(final Balance balance, final BankAccount bankAccount) {
    final Optional<Currency> curr = Currency.fromString(balance.currency());
    if (curr.isEmpty()) {
      log.warn("Unknown currency: {}", balance.currency());
    }

    return curr.map(currency -> BankAccountStatus.builder()
      .accountId(balance.acc())
      .accountName(bankAccount.name())
      .bankType(BANK_TYPE)
      .amount(privatBalanceAmountParser.parseAmount(balance.balanceOut()))
      .amountUah(privatBalanceAmountParser.parseAmount(balance.balanceOutEq()))
      .currency(currency)
      .updatedAt(clock.instant())
      .build()
    );
  }

  private boolean shouldProcessAccount(final Balance balance, final Collection<String> accounts) {
    final boolean shouldProcess = accounts.contains(balance.acc());
    if (!shouldProcess) {
      log.debug("Ignoring account: {} - {} {}", balance.acc(), balance.currency(), balance.balanceOut());
    }
    return shouldProcess;
  }
}
