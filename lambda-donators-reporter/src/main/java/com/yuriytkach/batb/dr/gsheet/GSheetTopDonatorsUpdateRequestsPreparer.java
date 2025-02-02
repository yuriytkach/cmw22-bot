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

package com.yuriytkach.batb.dr.gsheet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.yuriytkach.batb.common.google.SheetsCommonService;
import com.yuriytkach.batb.dr.Donator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;

@Slf4j
@ApplicationScoped
class GSheetTopDonatorsUpdateRequestsPreparer {

  @Inject
  SheetsCommonService sheetsCommonService;

  List<Request> prepareRequests(
    final Integer sheetId,
    final DonatorsTableUpdater.ProcessedDonators matchedDonators
  ) {
    final var requests = Stream.concat(
      createRequestsToAddNewDonators(sheetId, matchedDonators.newDonators()).stream(),
      createRequestsToUpdateExistingDonators(sheetId, matchedDonators.foundDonators()).stream()
    ).toList();

    final var cellsForColor = requests.stream()
      .map(request -> request.getUpdateCells().getStart().getRowIndex() + 1)
      .map(row -> new SheetsCommonService.Cell(row, 0))
      .toList();

    return Stream.concat(
      requests.stream(),
      sheetsCommonService.createSetCellColorRequests(sheetId, cellsForColor, DonatorsTableUpdater.ORANGE_COLOR).stream()
    ).toList();
  }

  private List<Request> createRequestsToUpdateExistingDonators(
    final Integer sheetId,
    final Map<ExistingDonator, Donator> existingDonatorDonatorMap
  ) {
    final var result = EntryStream.of(existingDonatorDonatorMap)
      .mapKeys(this::mapExistingToDonator)
      .filterKeyValue(this::shouldUpdateExistingDonator)
      .mapKeyValue((existing, donator) -> new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(existing.rowIndex() - 1).setColumnIndex(0))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(
            new ExtendedValue().setStringValue(donator.name())),

          new CellData().setUserEnteredValue(
            new ExtendedValue().setNumberValue((double) donator.count() + existing.count())),

          new CellData().setUserEnteredValue(
            new ExtendedValue().setNumberValue((double) (donator.amount() + existing.amount()) / 100)),

          createTxDateTimeCellData(donator.lastTxDateTime())
        ))))
        .setFields("userEnteredValue"))
      ).toImmutableList();

    log.info(
      "Existing donators to update: {} from total existing found: {}",
      result.size(),
      existingDonatorDonatorMap.size());

    return result;
  }

  private ExDonator mapExistingToDonator(final ExistingDonator existingDonator) {
    return new ExDonator(
      existingDonator.rowIndex(),
      existingDonator.name(),
      ((BigDecimal) existingDonator.columnValues().get(2)).longValue() * 100,
      ((BigDecimal) existingDonator.columnValues().get(1)).intValue(),
      sheetsCommonService.convertSheetsDateToInstant(((BigDecimal) existingDonator.columnValues().get(3)).doubleValue())
    );
  }

  private boolean shouldUpdateExistingDonator(final ExDonator existingDonator, final Donator donator) {
    log.trace("Last transaction date for {}: {}", existingDonator.name(), existingDonator.lastTxDateTime());
    return existingDonator.lastTxDateTime().plusSeconds(60).isBefore(donator.lastTxDateTime());
  }

  private List<Request> createRequestsToAddNewDonators(
    final Integer sheetId,
    final Map<Integer, Donator> rowDonatorMap
  ) {
    return EntryStream.of(rowDonatorMap)
      .mapKeyValue((rowIndex, donator) -> new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex - 1).setColumnIndex(0))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(donator.name())),
          new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((double) donator.count())),
          new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((double) donator.amount() / 100)),
          createTxDateTimeCellData(donator.lastTxDateTime())
        ))))
        .setFields("userEnteredValue"))
      ).toImmutableList();
  }

  private CellData createTxDateTimeCellData(final Instant instant) {
    return new CellData()
      .setUserEnteredValue(new ExtendedValue().setNumberValue(sheetsCommonService.convertInstantToSheetsDate(instant)));
  }

  public record ExDonator(int rowIndex, String name, Long amount, int count, Instant lastTxDateTime) { }
}
