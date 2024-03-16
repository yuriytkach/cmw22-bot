package com.yuriytkach.batb.bsu.bank.gsheet;

import java.util.List;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GSheetAllAccountsSyncService {

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  GSheetService gSheetService;

  public void updateAllAccountsTotals(final List<BankAccountStatus> allAccountBalances) {
    bankAccessStorage.getRegistryConfig().ifPresentOrElse(
      registryConfig -> sheetsFactory.getSheetsService()
        .ifPresent(sheets -> gSheetService.updateRegistryTotals(allAccountBalances, registryConfig, sheets)),
      () -> log.error("No registry config found for GSheet")
    );
  }
}
