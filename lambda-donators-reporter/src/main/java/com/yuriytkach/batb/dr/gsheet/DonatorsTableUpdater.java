package com.yuriytkach.batb.dr.gsheet;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Color;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.google.SheetsCommonService;
import com.yuriytkach.batb.common.google.SheetsFactory;
import com.yuriytkach.batb.common.storage.BankAccessStorage;
import com.yuriytkach.batb.dr.Donator;
import com.yuriytkach.batb.dr.DonatorsFilterMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;

@Slf4j
@ApplicationScoped
public class DonatorsTableUpdater {

  public static final int DONATIONS_SKIP_ROWS = 2;
  public static final String DONATIONS_SORT_COL = "B";

  public static final int TOP_DONATORS_SKIP_ROWS = 1;
  public static final String TOP_DONATORS_SORT_COL = "B";

  public static final int TRACK_MONTH_ADJUST_FOR_PREV = 1;
  public static final Color ORANGE_COLOR = new Color()
    .setRed(1.0f)
    .setGreen(0.8f)
    .setBlue(0.6f);

  @Inject
  Clock clock;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  DonatorsFilterMapper donatorsFilterMapper;

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  SheetsCommonService sheetsCommonService;

  @Inject
  GSheetColumnCalculator gSheetColumnCalculator;

  @Inject
  GSheetExistingDonatorsFinder gSheetExistingDonatorsFinder;

  @Inject
  GSheetDonationsUpdateRequestsPreparer gSheetDonationsUpdateRequestsPreparer;

  @Inject
  GSheetTopDonatorsUpdateRequestsPreparer gSheetTopDonatorsUpdateRequestsPreparer;

  public void updateDonatorsTable(final Set<Donator> donators) {
    bankAccessStorage.getDonatorsConfig().ifPresentOrElse(
      statsConfig -> sheetsFactory.getSheetsService()
        .ifPresent(sheets -> updateDonatorsSheets(statsConfig, sheets, donators)),
      () -> log.error("No stats config found for GSheet")
    );
  }

  private void updateDonatorsSheets(
    final BankAccount account,
    final Sheets sheets,
    final Set<Donator> donators
  ) {
    if (isAccountDataInvalid(account)) {
      log.error("GSheet Account has no properties or sheetName is not defined: {}", account);
    }



    trackDonationsWithAmounts(account, sheets, donators);
    trackFrequentDonators(account, sheets, donators);
  }

  private void trackFrequentDonators(final BankAccount account, final Sheets sheets, final Set<Donator> donators) {
    final String sheetDocId = account.id();
    final String sheetNameTop = account.properties().get("sheetNameTop");

    log.info("Tracking frequent donators in sheet: {}", sheetNameTop);

    sheetsCommonService.getSheetIdByName(sheets, sheetDocId, sheetNameTop)
      .ifPresentOrElse(
        sheetId -> gSheetExistingDonatorsFinder
          .findAndMatchDonators(sheets, sheetDocId, sheetNameTop, "A", TOP_DONATORS_SKIP_ROWS, donators, 4)
          .ifPresent(matchedDonators -> {
            log.info("Existing donators matched: {}", matchedDonators.foundDonators().size());
            log.debug("Existing donators matched: {}", matchedDonators.foundDonators());

            final ProcessedDonators finalMatchedDonators;

            if (matchedDonators.newDonators().isEmpty()) {
              finalMatchedDonators = matchedDonators;
            } else {
              final int startRowForNew = matchedDonators.newDonators().keySet().stream()
                .mapToInt(Integer::intValue).min().orElseThrow();

              final Set<Donator> finalNewDonators = donatorsFilterMapper
                .filterDonatorsByCount(matchedDonators.newDonators().values());

              log.info("New donators after filtering: {}", finalNewDonators.size());
              log.debug("New donators after filtering: {}", finalNewDonators);

              final var newDonatorsWithRows = EntryStream.of(List.copyOf(finalNewDonators))
                .mapKeys(row -> row + startRowForNew)
                .toImmutableMap();

              finalMatchedDonators = new ProcessedDonators(matchedDonators.foundDonators(), newDonatorsWithRows);
            }

            final var requests = gSheetTopDonatorsUpdateRequestsPreparer.prepareRequests(
              sheetId, finalMatchedDonators
            );
            if (!requests.isEmpty()) {
              sheetsCommonService.executeBatchGSheetRequests(sheets, sheetDocId, requests);
            }
            sheetsCommonService.sortSheetByColumn(
              sheets, sheetDocId, sheetId, TOP_DONATORS_SORT_COL, TOP_DONATORS_SKIP_ROWS);
          }),
        () -> log.warn("Sheet ID not found for sheet: {}", sheetNameTop)
      );

    log.info("Donations tracking completed in sheet: {}", sheetNameTop);
  }

  private void trackDonationsWithAmounts(final BankAccount account, final Sheets sheets, final Set<Donator> donators) {
    final Set<Donator> finalDonators = donatorsFilterMapper.filterDonatorsByAmount(donators);

    log.info("Donators after filtering: {}", finalDonators.size());
    log.debug("Donators after filtering: {}", finalDonators);

    final String sheetName = account.properties().get("sheetName");
    final String namesCol = account.properties().get("namesCol");
    final String monthColStart = account.properties().get("monthColStart");

    sheetsCommonService.getSheetIdByName(sheets, account.id(), sheetName)
      .ifPresentOrElse(
        sheetId -> findTrackColumnForPrevMonth(sheets, account.id(), sheetName, monthColStart)
          .ifPresentOrElse(
            trackCol -> trackDonations(
              sheets, account.id(), sheetName, sheetId, namesCol, trackCol, monthColStart, finalDonators),
            () -> log.warn("No tracking column was found")
          ),
        () -> log.warn("Sheet ID not found for sheet: {}", sheetName)
      );
  }

  private void trackDonations(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final int sheetId,
    final String namesCol,
    final String trackCol,
    final String trackStartCol,
    final Set<Donator> donators
  ) {
    log.info("Tracking donations in sheet: {}, names column: {}, track column: {}", sheetName, namesCol, trackCol);

    gSheetExistingDonatorsFinder
      .findAndMatchDonators(sheets, sheetDocId, sheetName, namesCol, DONATIONS_SKIP_ROWS, donators, 1)
      .ifPresent(matchedDonators -> {
        final var requests = gSheetDonationsUpdateRequestsPreparer.prepareRequests(
          sheetId, matchedDonators, namesCol, trackStartCol, trackCol
        );
        if (!requests.isEmpty()) {
          sheetsCommonService.executeBatchGSheetRequests(sheets, sheetDocId, requests);
        }
        sheetsCommonService.sortSheetByColumn(sheets, sheetDocId, sheetId, DONATIONS_SORT_COL, DONATIONS_SKIP_ROWS);
      });

    log.info("Donations tracking completed in sheet: {}", sheetName);
  }

  private Optional<String> findTrackColumnForPrevMonth(
    final Sheets sheets,
    final String sheetDocId,
    final String sheetName,
    final String monthColStart
  ) {
    return sheetsCommonService.readDateCell(sheets, sheetDocId, sheetName, monthColStart, 1).map(startDate -> {
      log.info("Tracking start date: {}", startDate);
      final String endTrackCol = gSheetColumnCalculator.calculateColumnId(
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

  record ProcessedDonators(
    Map<ExistingDonator, Donator> foundDonators,
    Map<Integer, Donator> newDonators
  ) { }

}
