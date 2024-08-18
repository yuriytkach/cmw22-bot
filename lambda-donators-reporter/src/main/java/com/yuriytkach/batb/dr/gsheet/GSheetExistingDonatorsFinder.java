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

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.yuriytkach.batb.common.google.SheetUtilities;
import com.yuriytkach.batb.dr.Donator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
class GSheetExistingDonatorsFinder {

  @Inject
  GSheetColumnCalculator gSheetColumnCalculator;

  @Inject
  SheetUtilities sheetUtilities;

  public Optional<DonatorsTableUpdater.ProcessedDonators> findAndMatchDonators(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String namesCol,
    final int namesSkipRows,
    final Set<Donator> donators,
    final int existingDonatorColumnsToRead
  ) {
    final var allDonators = findAllExisting(
      sheets, sheetDocId, sheetName, namesCol, namesSkipRows, existingDonatorColumnsToRead);

    if (allDonators.isEmpty()) {
      log.error("Cannot work with sheets without donators data in {}", sheetName);
      return Optional.empty();
    }

    final var maxDonatorsRow = allDonators.stream()
      .mapToInt(ExistingDonator::rowIndex)
      .max()
      .orElseThrow(() -> new IllegalStateException("No max row found in " + sheetName));

    log.debug("Max donators row in {}: {}", sheetName, maxDonatorsRow);

    final var allDonatorsMap = StreamEx.of(allDonators)
      .flatMapToEntry(donator -> Map.of(
        Optional.of(donator.name()), donator,
        donator.invertName(), donator
      ))
      .flatMapKeys(Optional::stream)
      .distinctKeys()
      .toImmutableMap();

    final var foundDonators = StreamEx.of(donators)
      .filter(donator -> allDonatorsMap.containsKey(donator.name()))
      .mapToEntry(donator -> allDonatorsMap.get(donator.name()))
      .invert()
      .toImmutableMap();
    log.info("Existing donators matched in {}: {}", sheetName, foundDonators.size());

    final AtomicInteger newDonatorsRow = new AtomicInteger(maxDonatorsRow);
    final var newDonators = StreamEx.of(donators)
      .filter(not(donator -> allDonatorsMap.containsKey(donator.name())))
      .mapToEntry(ignored -> newDonatorsRow.incrementAndGet())
      .invert()
      .toImmutableMap();
    log.info("New donators found in {}: {}", sheetName, newDonators.size());
    log.debug("New donators in {}: {}", sheetName, newDonators);
    return Optional.of(new DonatorsTableUpdater.ProcessedDonators(foundDonators, newDonators));
  }

  private List<ExistingDonator> findAllExisting(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String namesCol,
    final int skipRows,
    final int columnsToRead
  ) {
    final int endColIndex = sheetUtilities.columnToIndex(namesCol) + columnsToRead - 1;
    final String endCol = sheetUtilities.indexToColumn(endColIndex);

    try {
      final String range = "'%s'!%s%d:%s".formatted(sheetName, namesCol, skipRows + 1, endCol);
      log.debug("Reading all existing donators data in range: {}", range);

      final ValueRange response = sheets.spreadsheets().values()
        .get(sheetDocId, range)
        .setValueRenderOption("UNFORMATTED_VALUE")
        .execute();
      if (response == null || response.getValues() == null || response.getValues().isEmpty()) {
        log.warn("Cannot read donators in range: {}", range);
        return List.of();
      }

      final var result = EntryStream.of(response.getValues())
        .filterValues(list -> !list.isEmpty() && list.getFirst() != null && !list.getFirst().toString().isBlank())
        .mapKeys(row -> row + skipRows + 1)
        .mapKeyValue(ExistingDonator::new)
        .distinct(ExistingDonator::name)
        .toImmutableList();

      log.info("Found existing donators in {}: {}", sheetName, result.size());
      return result;
    } catch (final Exception ex) {
      log.error("Failed to read data from Google Sheets: {}", ex.getMessage(), ex);
      return List.of();
    }
  }

}
