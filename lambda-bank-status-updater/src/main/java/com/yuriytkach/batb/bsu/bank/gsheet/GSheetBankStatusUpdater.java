package com.yuriytkach.batb.bsu.bank.gsheet;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.MDC;

import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class GSheetBankStatusUpdater implements BankStatusUpdater {

  private static final BankType BANK_TYPE = BankType.GSHEET;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  AccountBalanceStorage accountBalanceStorage;

  @Inject
  GSheetService gSheetService;

  @Inject
  Clock clock;

  @Override
  public BankType bankType() {
    return BANK_TYPE;
  }

  @Override
  public Set<BankAccountStatus> updateAllAccountStatuses(final Map<String, BankAccountStatus> previousStatuses) {
    MDC.put("bankType", BANK_TYPE.name());
    final var configuredAccounts = readBankAccounts();
    log.info("Updating all account statuses for Gsheet: {}", configuredAccounts.size());

    return updateAccountStatuses(previousStatuses, configuredAccounts);
  }

  @Override
  public Set<BankAccountStatus> updateSpecifiedAccountStatuses(final Map<String, BankAccountStatus> specificStatuses) {
    final var accountsToUpdate = StreamEx.of(readBankAccounts())
      .filter(acc -> specificStatuses.containsKey(acc.id()))
      .toImmutableSet();

    log.info("Updating account statuses for GSheet: {}", accountsToUpdate.size());
    return updateAccountStatuses(specificStatuses, accountsToUpdate);
  }

  private Set<BankAccountStatus> updateAccountStatuses(
    final Map<String, BankAccountStatus> previousStatuses,
    final Set<BankAccount> accounts
  ) {
    return sheetsFactory.getSheetsService()
      .map(sheets -> {
        final var retrievedAccountStatuses = StreamEx.of(accounts)
          .mapToEntry(account -> gSheetService.readAccountStatus(account, sheets))
          .flatMapValues(Optional::stream)
          .mapKeyValue(this::buildBankAccountStatus)
          .toImmutableSet();
        log.info("Total bank account statuses retrieved: {}", retrievedAccountStatuses.size());

        final var updatedAccountStatuses = findUpdatedStatuses(previousStatuses, retrievedAccountStatuses);
        log.info("Total bank account statuses updated: {}", updatedAccountStatuses.size());

        accountBalanceStorage.saveAll(updatedAccountStatuses);

        return previousStatusesWithUpdated(previousStatuses, updatedAccountStatuses);
      })
      .orElseGet(() -> Set.copyOf(previousStatuses.values()));
  }

  private BankAccountStatus buildBankAccountStatus(
    final BankAccount bankAccount,
    final GSheetService.GSheetData gSheetData
  ) {
    return BankAccountStatus.builder()
      .accountId(bankAccount.id())
      .accountName(bankAccount.name())
      .bankType(BANK_TYPE)
      .amount(gSheetData.raised())
      .amountUah(gSheetData.raisedUah())
      .currency(gSheetData.currency())
      .spentAmount(gSheetData.spent())
      .spentAmountUah(gSheetData.spentUah())
      .updatedAt(clock.instant())
      .ignoreForTotal(true)
      .build();
  }

  private Set<BankAccount> readBankAccounts() {
    return StreamEx.of(bankAccessStorage.getListOfAccounts(BANK_TYPE))
      .distinct(BankAccount::id)
      .toImmutableSet();
  }
}
