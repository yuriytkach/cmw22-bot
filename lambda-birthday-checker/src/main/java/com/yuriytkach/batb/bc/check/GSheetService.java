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

package com.yuriytkach.batb.bc.check;

import java.time.LocalDate;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.CellData;
import com.yuriytkach.batb.bc.Person;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.google.SheetUtilities;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GSheetService {

  private final SheetUtilities sheetUtilities;

  public List<Person> readPeople(final BankAccount account, final Sheets sheets) {
    if (isAccountDataInvalid(account)) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);
      return List.of();
    }

    final String sheetName = account.properties().get("peopleSheet");
    final String birthdayCol = account.properties().get("peopleBirthdayCol");
    final String imageCol = account.properties().get("peoplePhotoCol");

    final int birthdayColIndex = sheetUtilities.columnToIndex(birthdayCol);
    final int imageColIndex = sheetUtilities.columnToIndex(imageCol);

    try {
      final String range = "'%s'!A:%s".formatted(sheetName, imageCol);
      log.debug("Reading people from sheet for range {}", range);

      final var spreadsheet = sheets.spreadsheets()
        .get(account.id())
        .setRanges(List.of(range))
        .setIncludeGridData(true)
        .execute();

      final var sheet = spreadsheet.getSheets().getFirst();
      final var gridData = sheet.getData().getFirst();
      log.debug("Read rows: {}", gridData.getRowData().size());

      return gridData.getRowData().stream()
        .skip(1) // skip header row
        .filter(row -> isValidPersonRow(row.getValues(), birthdayColIndex))
        .map(row -> new Person(
          row.getValues().getFirst().getFormattedValue(),
          LocalDate.parse(row.getValues().get(birthdayColIndex).getFormattedValue()),
          row.getValues().size() > imageColIndex ? row.getValues().get(imageColIndex).getHyperlink() : null
        ))
        .toList();
    } catch (final Exception ex) {
      log.error("Failed to read persons from Google Sheets: {}", ex.getMessage(), ex);
      return List.of();
    }
  }

  private boolean isValidPersonRow(final List<CellData> row, final int birthdayColIndex) {
    return row.getFirst().getFormattedValue() != null
      && !row.getFirst().getFormattedValue().isBlank()
      && row.size() > birthdayColIndex
      && row.get(birthdayColIndex).getFormattedValue() != null
      && !row.get(birthdayColIndex).getFormattedValue().isBlank();
  }

  private boolean isAccountDataInvalid(final BankAccount account) {
    return account.properties() == null
      || account.properties().isEmpty()
      || !account.properties().containsKey("peopleSheet")
      || !account.properties().containsKey("peopleBirthdayCol");
  }

}
