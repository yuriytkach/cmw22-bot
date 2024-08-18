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

package com.yuriytkach.batb.bsu.stats;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

import com.google.api.services.sheets.v4.Sheets;
import com.yuriytkach.batb.bsu.google.GSheetService;
import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.Statistic;
import com.yuriytkach.batb.common.StatisticData;
import com.yuriytkach.batb.common.storage.StatisticStorage;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class GSheetStatsUpdater {

  private final GSheetService gSheetService;
  private final StatisticStorage statisticStorage;
  private final StatsIdMapper statsIdMapper;
  private final Clock clock;

  public void updateStats(final BankAccount statsConfig, final Sheets sheets) {
    gSheetService.readStats(statsConfig, sheets)
      .ifPresentOrElse(
        gSheetStatsData -> processStats(gSheetStatsData, statsConfig.properties()),
        () -> log.warn("No stats found for GSheet")
      );
  }

  private void processStats(final GSheetService.GSheetStatsData gSheetStatsData, final Map<String, String> properties) {
    log.debug("Loaded stats: {}", gSheetStatsData);
    final long total = StreamEx.of(gSheetStatsData.stats())
      .mapToLong(GSheetService.StatsDataRecord::amount)
      .sum();

    final Optional<Statistic> prevStats = statisticStorage.get();
    if (prevStats.filter(stats -> stats.totalAmount() == total).isPresent()) {
      log.info("Stats are the same, no need to update");
      return;
    } else {
      log.info(
        "Stats are different. Old total: {}. New total: {}",
        prevStats.map(Statistic::totalAmount).orElse(0L),
        total
      );
    }

    final var stats = StreamEx.of(gSheetStatsData.stats())
      .map(stat -> createStatsRecord(stat, total))
      .toList();

    final var totalStatistic = new Statistic(stats, total, clock.instant());

    statisticStorage.save(totalStatistic);
  }

  private StatisticData createStatsRecord(final GSheetService.StatsDataRecord stat, final long total) {
    return StatisticData.builder()
      .id(statsIdMapper.mapNameToId(stat.name()))
      .count(stat.count())
      .amount(stat.amount())
      .amountPercent(Math.round((double) stat.amount() * 100.0 / total))
      .build();
  }

}
