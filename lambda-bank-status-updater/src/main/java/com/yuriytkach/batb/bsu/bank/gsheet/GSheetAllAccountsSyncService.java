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

package com.yuriytkach.batb.bsu.bank.gsheet;

import java.util.List;

import com.yuriytkach.batb.bsu.google.GSheetService;
import com.yuriytkach.batb.common.google.SheetsFactory;
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
