package com.yuriytkach.batb.dr.gsheet;

import static java.util.function.Predicate.not;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.google.SheetsCommonService;
import com.yuriytkach.batb.common.google.SheetsFactory;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;

@Slf4j
@ApplicationScoped
public class DonatorsTableUpdater {

  public static final int NAMES_START_ROW = 3;
  public static final int TRACK_MONTH_ADJUST_FOR_PREV = -1;

  @Inject
  Clock clock;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  SheetsCommonService sheetsCommonService;

  @Inject
  GSheetDateColumnCalculator gSheetDateColumnCalculator;

  public void updateDonatorsTable(final Map<String, Long> donators) {
    bankAccessStorage.getDonatorsConfig().ifPresentOrElse(
      statsConfig -> sheetsFactory.getSheetsService()
        .ifPresent(sheets -> updateDonatorsSheets(statsConfig, sheets, donators)),
      () -> log.error("No stats config found for GSheet")
    );
  }

  private void updateDonatorsSheets(
    final BankAccount account,
    final Sheets sheets,
    final Map<String, Long> donators
  ) {
    if (isAccountDataInvalid(account)) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);;
    }

    final String sheetName = account.properties().get("sheetName");
    final String namesCol = account.properties().get("namesCol");
    final String monthColStart = account.properties().get("monthColStart");

    findTrackColumnForPrevMonth(sheets, account.id(), sheetName, monthColStart)
      .ifPresentOrElse(
        trackCol -> trackDonations(sheets, account.id(), sheetName, namesCol, trackCol, monthColStart, donators),
        () -> log.warn("No tracking column was found")
      );
  }

  private void trackDonations(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String namesCol,
    final String trackCol,
    final String trackStartCol,
    final Map<String, Long> donators
  ) {
    log.info("Tracking donations in sheet: {}, names column: {}, track column: {}", sheetName, namesCol, trackCol);

    final var allDonators = findAllDonators(sheets, sheetDocId, sheetName, namesCol);
    log.info("Found all donators: {}", allDonators.size());

    final var maxDonatorsRow = allDonators.values().stream().mapToInt(Integer::intValue).max().orElse(1000);
    log.debug("Max donators row: {}", maxDonatorsRow);

    final var foundDonators = EntryStream.of(donators)
      .filterKeys(allDonators::containsKey)
      .mapKeys(allDonators::get)
      .toImmutableMap();
    log.info("Existing donators found: {}", foundDonators.size());

    final var newDonators = EntryStream.of(donators)
      .filterKeys(not(allDonators::containsKey))
      .toImmutableMap();
    log.info("New donators found: {}", newDonators.size());
    log.debug("New donators: {}", newDonators);

    sheetsCommonService.getSheetIdByName(sheets, sheetDocId, sheetName).ifPresentOrElse(
      sheetId -> {
        log.trace("Sheet ID found: {}", sheetId);
        final AtomicInteger newDonatorsRow = new AtomicInteger(maxDonatorsRow);
        final var newNamesCells = EntryStream.of(newDonators)
          .sorted(Map.Entry.comparingByKey())
          .mapValues(ignored -> newDonatorsRow.incrementAndGet())
          .invert()
          .toImmutableMap();

        newDonatorsRow.set(maxDonatorsRow);
        final var donationCells = EntryStream.of(newDonators)
          .sorted(Map.Entry.comparingByKey())
          .mapKeys(ignored -> newDonatorsRow.incrementAndGet())
          .append(foundDonators)
          .toImmutableMap();

        updateCells(sheets, sheetDocId, sheetId, donationCells, trackCol, newNamesCells, namesCol, trackStartCol);

        sheetsCommonService.sortSheetByColumn(sheets, sheetDocId, sheetId, "B", 2);
      },
      () -> log.warn("Sheet ID not found for sheet: {}", sheetName)
    );

    log.info("Donations tracking completed");
  }

  private Map<String, Integer> findAllDonators(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String namesCol
  ) {
    try {
      final String range = "'%s'!%s%d:%s".formatted(sheetName, namesCol, NAMES_START_ROW, namesCol);
      log.debug("Reading all names from range: {}", range);
      final ValueRange response = sheets.spreadsheets().values()
        .get(sheetDocId, range)
        .execute();
      return EntryStream.of(response.getValues())
        .mapValues(list -> list.getFirst().toString())
        .mapKeys(row -> row + NAMES_START_ROW)
        .invert()
        .distinctKeys()
        .toImmutableMap();
    } catch (final Exception ex) {
      log.error("Failed to read data from Google Sheets: {}", ex.getMessage(), ex);
      return Map.of();
    }
  }

  private Optional<String> findTrackColumnForPrevMonth(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String monthColStart
  ) {
    return sheetsCommonService.readDateCell(sheets, sheetDocId, sheetName, monthColStart, 1).map(startDate -> {
      log.info("Tracking start date: {}", startDate);
      final String endTrackCol = gSheetDateColumnCalculator.calculateColumnId(
        monthColStart, startDate, LocalDate.now(clock).minusMonths(TRACK_MONTH_ADJUST_FOR_PREV)
      );
      log.debug("Found tracking end column: {}", endTrackCol);
      final var endDate = sheetsCommonService.readDateCell(sheets, sheetDocId, sheetName, endTrackCol, 1);
      log.info("Tracking end date: {}", endDate);

      return endTrackCol;
    });
  }

  private boolean isAccountDataInvalid(final BankAccount account) {
    return account.properties() == null
      || account.properties().isEmpty()
      || !account.properties().containsKey("sheetName");
  }

  public void updateCells(
    final Sheets sheetsService,
    final String sheetDocId,
    final int sheetId,
    final Map<Integer, Long> values,
    final String valuesColumn,
    final Map<Integer, String> names,
    final String namesColumn,
    final String trackStartColumn
  ) {
    try {
      final List<Request> requests = new ArrayList<>();

      // Determine column index based on column letter (e.g., 'A' -> 0)
      final int valueColumnIndex = valuesColumn.charAt(0) - 'A';
      final int namesColumnIndex = namesColumn.charAt(0) - 'A';

      names.forEach((rowIndex, name) -> requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex - 1).setColumnIndex(namesColumnIndex))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(new ExtendedValue().setStringValue(name)),
          new CellData().setUserEnteredValue(new ExtendedValue()
            .setFormulaValue("=SUM(%s%d:%d)".formatted(trackStartColumn, rowIndex, rowIndex)))
        ))))
        .setFields("userEnteredValue"))
      ));

      values.forEach((rowIndex, value) -> requests.add(new Request().setUpdateCells(new UpdateCellsRequest()
        .setStart(new GridCoordinate().setSheetId(sheetId).setRowIndex(rowIndex - 1).setColumnIndex(valueColumnIndex))
        .setRows(List.of(new RowData().setValues(List.of(
          new CellData().setUserEnteredValue(new ExtendedValue().setNumberValue((double) value / 100))
        ))))
        .setFields("userEnteredValue"))
      ));

      EntryStream.of(values)
        .mapValues(ignored -> valueColumnIndex)
        .append(EntryStream.of(names).mapValues(ignored -> namesColumnIndex))
        .forKeyValue((rowIndex, colIndex) -> requests.add(new Request().setRepeatCell(new RepeatCellRequest()
          .setRange(new GridRange()
            .setSheetId(sheetId)
            .setStartRowIndex(rowIndex - 1)
            .setEndRowIndex(rowIndex)
            .setStartColumnIndex(colIndex)
            .setEndColumnIndex(colIndex + 1))
          .setCell(new CellData()
            .setUserEnteredFormat(new CellFormat()
              .setBackgroundColor(new Color()
                .setRed(1.0f)
                .setGreen(0.8f)
                .setBlue(0.6f))))
          .setFields("userEnteredFormat.backgroundColor")
        )));

      final BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
      sheetsService.spreadsheets().batchUpdate(sheetDocId, batchRequest).execute();
    } catch (final Exception ex) {
      log.error("Failed to write data to Google Sheets: {}", ex.getMessage(), ex);
    }
  }
}
