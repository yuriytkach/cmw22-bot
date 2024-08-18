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
