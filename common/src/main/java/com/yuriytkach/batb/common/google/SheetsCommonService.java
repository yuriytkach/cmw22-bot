package com.yuriytkach.batb.common.google;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SortRangeRequest;
import com.google.api.services.sheets.v4.model.SortSpec;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class SheetsCommonService {

  // Google Sheets: serial date 1 is December 30, 1899
  public static final LocalDate GSHEET_BASE_DATE = LocalDate.of(1899, 12, 30);

  public Optional<Integer> getSheetIdByName(final Sheets sheets, final String sheetDocId, final String sheetName) {
    try {
      log.debug("Getting sheet ID by name: {}", sheetName);
      final Spreadsheet spreadsheet = sheets.spreadsheets().get(sheetDocId)
        .setFields("sheets(properties(sheetId,title))")
        .execute();

      final var result = StreamEx.of(spreadsheet.getSheets())
        .filter(sheet -> sheet.getProperties().getTitle().equals(sheetName))
        .map(sheet -> sheet.getProperties().getSheetId())
        .findFirst();

      log.info("Google Sheet ID by name '{}': {}", sheetName, result);
      return result;
    } catch (final Exception ex) {
      log.error("Failed to write data to Google Sheets: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  public Optional<LocalDate> readDateCell(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String col,
    final int row
  ) {
    try {
      final String range = "'%s'!%s%d".formatted(sheetName, col, row);
      log.debug("Reading date cell value from: {}", range);
      final ValueRange response = sheets.spreadsheets().values()
        .get(sheetDocId, range)
        .setValueRenderOption("UNFORMATTED_VALUE")
        .execute();
      final Object object = response.getValues().getFirst().getFirst();
      log.info("Date cell value from '{}': {}", range, object);
      return Optional.of(convertSerialDateToLocalDate(((BigDecimal) object).longValue()));
    } catch (final Exception ex) {
      log.error("Failed to read data from Google Sheets: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  public void sortSheetByColumn(
    final Sheets sheets,
    final String sheetDocId,
    final int sheetId,
    final String sortColumn,
    final int skipRows
  )  {
    try {
      final int sortColumnIndex = sortColumn.charAt(0) - 'A';

      log.info("Sorting sheet by column: {} [{}], skip rows: {}", sortColumn, sortColumnIndex, skipRows);

      final SortSpec sortSpec = new SortSpec()
        .setDimensionIndex(sortColumnIndex)
        .setSortOrder("DESCENDING");

      final GridRange sortRange = new GridRange()
        .setSheetId(sheetId)
        .setStartRowIndex(skipRows)
        .setStartColumnIndex(0);

      final Request request = new Request().setSortRange(new SortRangeRequest()
        .setRange(sortRange)
        .setSortSpecs(Collections.singletonList(sortSpec)));

      final BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
        .setRequests(Collections.singletonList(request));

      final BatchUpdateSpreadsheetResponse response = sheets.spreadsheets()
        .batchUpdate(sheetDocId, batchRequest)
        .execute();
      log.trace("Sorting response: {}", response);
    } catch (final Exception ex) {
      log.error("Failed to sort data in Google Sheets: {}", ex.getMessage(), ex);
    }
  }

  private LocalDate convertSerialDateToLocalDate(final long serialDate) {
    return GSHEET_BASE_DATE.plusDays(serialDate);
  }

}
