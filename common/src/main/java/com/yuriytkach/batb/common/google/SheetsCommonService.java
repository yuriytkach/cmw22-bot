package com.yuriytkach.batb.common.google;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
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
  public static final ZonedDateTime GOOGLE_EPOCH_START = ZonedDateTime.of(
    GSHEET_BASE_DATE, LocalTime.of(0, 0, 0, 0), ZoneId.of("UTC"));

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

  public List<Request> createSetCellColorRequests(
    final int sheetId,
    final List<Cell> cellsForColor,
    final Color color
  ) {
    return StreamEx.of(cellsForColor)
      .map(cell -> new Request().setRepeatCell(new RepeatCellRequest()
        .setRange(new GridRange()
          .setSheetId(sheetId)
          .setStartRowIndex(cell.row() - 1)
          .setEndRowIndex(cell.row())
          .setStartColumnIndex(cell.col())
          .setEndColumnIndex(cell.col() + 1))
        .setCell(new CellData().setUserEnteredFormat(new CellFormat().setBackgroundColor(color)))
        .setFields("userEnteredFormat.backgroundColor")
      ))
      .toImmutableList();
  }

  public void executeBatchGSheetRequests(
    final Sheets sheets,
    final String sheetDocId,
    final List<Request> requests
  ) {
    try {
      log.debug("Executing gsheet batch requests: {}", requests.size());
      final BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);

      final BatchUpdateSpreadsheetResponse response = sheets.spreadsheets()
        .batchUpdate(sheetDocId, batchRequest)
        .execute();
      log.trace("Sheets batch response: {}", response);
    } catch (final Exception ex) {
      log.error("Failed to write data to Google Sheets: {}", ex.getMessage(), ex);
    }
  }

  public void sortSheetByColumn(
    final Sheets sheets,
    final String sheetDocId,
    final int sheetId,
    final String sortColumn,
    final int skipRows
  )  {
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

    executeBatchGSheetRequests(sheets, sheetDocId, List.of(request));
  }

  // Function to convert an Instant to a Google Sheets serial date number
  public double convertInstantToSheetsDate(final Instant instant) {
    // Convert Instant to ZonedDateTime at UTC
    final ZonedDateTime dateTime = instant.atZone(ZoneId.of("UTC"));

    // Calculate difference in days
    final long daysSinceGoogleEpoch = ChronoUnit.DAYS.between(GOOGLE_EPOCH_START, dateTime);

    // Since Google Sheets also counts partial days as decimal values, calculate seconds past midnight
    final long secondsPastMidnight = (dateTime.toLocalTime().toSecondOfDay());
    final double additionalDays = secondsPastMidnight / 86400.0;  // Convert seconds to fraction of a day

    return daysSinceGoogleEpoch + additionalDays;
  }

  public Instant convertSheetsDateToInstant(final double sheetsDate) {
    // Extract the full days and the fractional part of the day
    final long fullDays = (long) sheetsDate;
    final double fractionalDay = sheetsDate - fullDays;

    // Calculate the total seconds represented by the fractional day
    final long secondsInDay = (long) (fractionalDay * 86400);

    // Add full days and seconds to the epoch
    final ZonedDateTime dateTime = GOOGLE_EPOCH_START.plusDays(fullDays).plusSeconds(secondsInDay);

    return dateTime.toInstant();
  }

  private LocalDate convertSerialDateToLocalDate(final long serialDate) {
    return GSHEET_BASE_DATE.plusDays(serialDate);
  }

  public record Cell(int row, int col) { }
}
