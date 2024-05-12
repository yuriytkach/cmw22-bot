package com.yuriytkach.batb.dr.gsheet;

import java.util.List;
import java.util.Map;

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
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
class GSheetDonationsUpdateRequestsPreparer {

  @Inject
  SheetsCommonService sheetsCommonService;

  List<Request> prepareRequests(
    final Integer sheetId,
    final DonatorsTableUpdater.ProcessedDonators matchedDonators,
    final String namesCol,
    final String trackStartCol,
    final String trackCol
  ) {
    final var newNamesCells = EntryStream.of(matchedDonators.newDonators())
      .mapValues(Donator::name)
      .toImmutableMap();

    final var donationCells = EntryStream.of(matchedDonators.foundDonators())
      .mapKeys(ExistingDonator::rowIndex)
      .append(matchedDonators.newDonators())
      .mapValues(Donator::amount)
      .toImmutableMap();

    // Determine column index based on column letter (e.g., 'A' -> 0)
    final int valueColumnIndex = trackCol.charAt(0) - 'A';
    final int namesColumnIndex = namesCol.charAt(0) - 'A';

    final var cellsForColor = EntryStream.of(donationCells)
      .mapValues(ignored -> valueColumnIndex)
      .append(EntryStream.of(newNamesCells).mapValues(ignored -> namesColumnIndex))
      .toImmutableMap();

    return StreamEx.of(
        createRequestToAddNewDonators(sheetId, trackStartCol, newNamesCells, namesColumnIndex),
        createRequestsToUpdateExistingDonators(sheetId, donationCells, valueColumnIndex),
        sheetsCommonService.createSetCellColorRequests(sheetId, cellsForColor, DonatorsTableUpdater.ORANGE_COLOR)
      )
      .flatMap(List::stream)
      .toList();
  }

  private List<Request> createRequestsToUpdateExistingDonators(
    final Integer sheetId,
    final Map<Integer, Long> donationCells,
    final int valueColumnIndex
  ) {
    return EntryStream.of(donationCells)
      .mapKeyValue((rowIndex, value) -> new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex - 1).setColumnIndex(valueColumnIndex))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((double) value / 100))
        ))))
        .setFields("userEnteredValue"))
      ).toImmutableList();
  }

  private List<Request> createRequestToAddNewDonators(
    final Integer sheetId,
    final String trackStartCol,
    final Map<Integer, String> newNamesCells,
    final int namesColumnIndex
  ) {
    return EntryStream.of(newNamesCells)
      .mapKeyValue((rowIndex, name) -> new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex - 1).setColumnIndex(namesColumnIndex))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(name)),
          new CellData().setUserEnteredValue(new ExtendedValue()
            .setFormulaValue("=SUM(%s%d:%d)".formatted(trackStartCol, rowIndex, rowIndex)))
        ))))
        .setFields("userEnteredValue"))
      ).toImmutableList();
  }
}
