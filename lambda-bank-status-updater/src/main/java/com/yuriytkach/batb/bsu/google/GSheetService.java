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

package com.yuriytkach.batb.bsu.google;

import static com.yuriytkach.batb.common.Currency.UAH;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.Currency;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class GSheetService {

  private final NumberFormat format = NumberFormat.getInstance(new Locale("uk", "UA"));

  public Optional<GSheetAccountData> readAccountStatus(final BankAccount account, final Sheets sheets) {
    if (isAccountDataInvalid(account)) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);
      return Optional.empty();
    }

    try {
      log.info("Reading google sheet {}, raised cell: {}, spent cell: {}",
        account.id(), account.properties().get("raisedCell"), account.properties().get("spentCell"));

      final String sheetName = account.properties().get("sheetName");

      final Optional<String> raisedStr = readValue(sheets, account.id(), sheetName,
        account.properties().get("raisedCell"));
      final Optional<String> raisedStrUah = readValue(sheets, account.id(), sheetName,
        account.properties().get("raisedUahCell"));

      final Optional<String> spentStr = readValue(sheets, account.id(), sheetName,
        account.properties().get("spentCell"));
      final Optional<String> spentStrUah = readValue(sheets, account.id(), sheetName,
        account.properties().get("spentUahCell"));

      final Optional<String> currStr = readValue(
        sheets, account.id(), sheetName, account.properties().get("currencyCell")
      );

      final Currency currency = currStr.flatMap(Currency::fromString).orElse(UAH);
      final Long raised = raisedStr.map(this::parseLong).orElse(0L);
      final Long spent = spentStr.map(this::parseLong).orElse(0L);

      final var result = GSheetAccountData.builder()
        .sheetId(account.id())
        .raised(raised)
        .raisedUah(raisedStrUah.map(this::parseLong).orElse(currency == UAH ? raised : 0))
        .spent(spent)
        .spentUah(spentStrUah.map(this::parseLong).orElse(currency == UAH ? spent : 0))
        .currency(currency)
        .build();
      log.debug("Read account status: {}", result);

      return Optional.of(result);

    } catch (final Exception ex) {
      log.error("Failed to read account status from Google Sheets: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  public Optional<GSheetStatsData> readStats(final BankAccount account, final Sheets sheets) {
    if (isAccountDataInvalid(account)) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);
      return Optional.empty();
    }

    final String sheetName = account.properties().get("sheetName");
    final String range = account.properties().get("dataRange");
    final int maxCol = Integer.parseInt(account.properties().get("maxCol"));

    log.debug("Reading cells in range: {} for sheet:{}", range, sheetName);

    try {
      final ValueRange response = sheets.spreadsheets().values()
        .get(account.id(), "'%s'!%s".formatted(sheetName, range))
        .execute();

      final List<List<Object>> values = response.getValues();

      if (values == null || values.isEmpty()) {
        log.debug("No data found in sheet {} for range {}", sheetName, range);
        return Optional.empty();
      } else {
        final var resultList = values.stream()
          .filter(objects -> isValidStatDataRow(objects, maxCol))
          .map(row -> new StatsDataRecord(
            String.valueOf(row.get(0)),
            Optional.ofNullable(row.get(3)).map(this::parseLongStrip).orElse(0L),
            Optional.ofNullable(row.get(4)).map(this::parseLongStrip).orElse(0L)
          ))
          .toList();

        return Optional.of(new GSheetStatsData(resultList));
      }
    } catch (final Exception ex) {
      log.error("Failed to read stats from Google Sheets: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  private Long parseLongStrip(final Object obj) {
    final var str = String.valueOf(obj).strip().replaceAll("\\D", "");
    return Long.parseLong(str);
  }

  private boolean isValidStatDataRow(final List<Object> objects, final int maxCols) {
    return objects != null && objects.size() >= maxCols;
  }

  @SneakyThrows
  private Long parseLong(final String value) {
    return format.parse(value).longValue() * 100;
  }

  private Optional<String> readValue(
    final Sheets sheets,
    final String sheetId,
    final String sheetName,
    final String cell
  )
    throws IOException {
    if (cell == null || cell.isBlank()) {
      return Optional.empty();
    }
    final String range = "%s!%s".formatted(sheetName, cell);
    log.debug("Reading cell: {}", range);
    final ValueRange response = sheets.spreadsheets().values()
      .get(sheetId, range)
      .execute();

    final List<List<Object>> values = response.getValues();

    if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
      log.debug("No data found in sheet {} for cell {}", sheetName, cell);
      return Optional.empty();
    } else {
      return Optional.ofNullable(values.get(0).get(0)).map(String.class::cast);
    }
  }

  public void updateRegistryTotals(
    final Collection<BankAccountStatus> allAccountBalances,
    final BankAccount registryConfig,
    final Sheets sheets
  ) {
    log.info("Updating registry totals for accounts: {}", allAccountBalances.size());

    final String sheetName = registryConfig.properties().get("sheetName");
    final String colAccount = registryConfig.properties().get("accountCol");
    final String colAmountUah = registryConfig.properties().get("amountUahCol");
    final String colAmount = registryConfig.properties().get("amountCol");
    final String colCurr = registryConfig.properties().get("currCol");
    final String colUpdatedAt = registryConfig.properties().get("updatedAtCol");
    final String colHumanName = registryConfig.properties().get("humanNameCol");

    final var allRangesToUpdate = StreamEx.of(allAccountBalances)
      .filter(acc -> acc.gsheetStatRow() != null)
      .flatMap(acc -> {
        final var accIdValueRange = buildValueRange(
          sheetName, colAccount, acc.gsheetStatRow(), acc.accountId()
        );

        final var amountUahValueRange = buildValueRange(
          sheetName, colAmountUah, acc.gsheetStatRow(), acc.amountUah() / 100.0
        );

        final var amountValueRange = buildValueRange(
          sheetName, colAmount, acc.gsheetStatRow(), acc.amount() / 100.0
        );

        final var currValueRange = buildValueRange(
          sheetName, colCurr, acc.gsheetStatRow(), acc.currency().toString()
        );

        final var updatedAtValueRange = buildValueRange(
          sheetName, colUpdatedAt, acc.gsheetStatRow(), acc.updatedAt().toString()
        );

        final var humanNameValueRange = buildValueRange(
          sheetName, colHumanName, acc.gsheetStatRow(), acc.accountName()
        );

        return Stream.of(
          accIdValueRange,
          amountUahValueRange, amountValueRange, currValueRange,
          updatedAtValueRange, humanNameValueRange
        );
      })
      .toList();

    if (allRangesToUpdate.isEmpty()) {
      log.warn("No data to update in Registry of Google Sheets");
    } else {
      log.info("Updating registry totals in Google Sheets: {}", allRangesToUpdate.size());

      final var batchRequest = new BatchUpdateValuesRequest();
      batchRequest.setData(allRangesToUpdate);
      batchRequest.setValueInputOption("RAW");

      try {
        sheets.spreadsheets().values()
          .batchUpdate(registryConfig.id(), batchRequest)
          .execute();
      } catch (final IOException ex) {
        log.error("Failed to update registry totals in Google Sheets: {}", ex.getMessage(), ex);
      }
    }
  }

  private ValueRange buildValueRange(
    final String sheetName,
    final String col,
    final String row,
    final Object value
  ) {
    final var amountUahValueRange = new ValueRange();
    amountUahValueRange.setRange("%s!%s%s".formatted(sheetName, col, row));
    amountUahValueRange.setValues(List.of(List.of(value)));
    return amountUahValueRange;
  }

  private boolean isAccountDataInvalid(final BankAccount account) {
    return account.properties() == null
      || account.properties().isEmpty()
      || !account.properties().containsKey("sheetName");
  }

  @Builder
  @RegisterForReflection
  public record GSheetAccountData(
    String sheetId, long raised, long raisedUah, long spent, long spentUah, Currency currency
  ) { }

  public record GSheetStatsData(List<StatsDataRecord> stats) {}

  public record StatsDataRecord(String name, long count, long amount) {}
}
