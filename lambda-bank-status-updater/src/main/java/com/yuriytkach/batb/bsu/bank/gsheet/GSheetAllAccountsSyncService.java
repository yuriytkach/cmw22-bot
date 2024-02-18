package com.yuriytkach.batb.bsu.bank.gsheet;

import java.util.List;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GSheetAllAccountsSyncService {

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  GSheetService gSheetService;

  public void updateAllAccountsTotals(final List<BankAccountStatus> allAccountBalances) {
    final List<String> tokens = bankAccessStorage.getListOfTokens(BankType.GSHEET);
    log.info("Loaded tokens: {}", tokens.size());

    if (tokens.isEmpty()) {
      log.warn("No tokens found for GSheet");
    } else {
      final String token = tokens.stream().findFirst().orElseThrow();

      bankAccessStorage.getRegistryConfig().ifPresentOrElse(
        registryConfig -> gSheetService.getSheetsService(token)
          .ifPresent(sheets -> gSheetService.updateRegistryTotals(allAccountBalances, registryConfig, sheets)),
        () -> log.error("No registry config found for GSheet")
      );
    }
  }
}
