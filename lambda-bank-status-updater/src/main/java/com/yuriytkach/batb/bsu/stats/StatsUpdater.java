package com.yuriytkach.batb.bsu.stats;

import com.yuriytkach.batb.common.google.SheetsFactory;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StatsUpdater {

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  GSheetStatsUpdater gSheetStatsUpdater;

  public void update() {
    bankAccessStorage.getStatsConfig().ifPresentOrElse(
      statsConfig -> sheetsFactory.getSheetsService()
        .ifPresent(sheets -> gSheetStatsUpdater.updateStats(statsConfig, sheets)),
      () -> log.error("No stats config found for GSheet")
    );
  }
}
