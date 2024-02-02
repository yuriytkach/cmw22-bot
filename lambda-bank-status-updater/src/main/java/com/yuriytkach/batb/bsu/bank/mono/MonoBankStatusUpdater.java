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
  public void updateAccountStatuses(final Map<String, BankAccountStatus> oldAccountStatuses) {
    MDC.put("bankType", BANK_TYPE.name());
    log.info("Updating account statuses for Monobank");

    final Set<BankAccount> accounts = readBankAccounts();

    final var retrievedAccountStatuses = StreamEx.of(accounts)
      .mapToEntry(account -> monoService.readJarStatus(account.id()))
      .flatMapValues(Optional::stream)
      .mapKeyValue(this::buildBankAccountStatus)
      .flatMap(Optional::stream)
      .toImmutableSet();
    log.info("Total bank account statuses retrieved: {}", retrievedAccountStatuses.size());

    final var updatedAccountStatuses = findUpdatedStatuses(oldAccountStatuses, retrievedAccountStatuses);
    log.info("Total bank account statuses updated: {}", updatedAccountStatuses.size());

    accountBalanceStorage.saveAll(updatedAccountStatuses);
  }

  private Set<BankAccount> readBankAccounts() {
    return StreamEx.of(bankAccessStorage.getListOfAccounts(BANK_TYPE))
      .distinct(BankAccount::id)
      .toImmutableSet();
  }

  private Optional<BankAccountStatus> buildBankAccountStatus(final BankAccount bankAccount, final MonoJar monoJar) {
    final Optional<Currency> curr = Currency.fromIsoCode(monoJar.currency());
    if (curr.isEmpty()) {
      log.warn("Unknown currency: {}. Cannot map account: {}", monoJar.currency(), bankAccount);
    }
    return curr.map(currency -> BankAccountStatus.builder()
      .accountId(bankAccount.id())
      .accountName("%s (%s)".formatted(bankAccount.name(), monoJar.ownerName()))
      .bankType(BANK_TYPE)
      .amount(monoJar.amount())
      .amountUah(monoJar.amount())
      .currency(currency)
      .updatedAt(clock.instant())
      .build()
    );
  }
}
