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

package com.yuriytkach.batb.dr.stats;

import static java.util.function.Predicate.not;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.yuriytkach.batb.dr.Donator;
import com.yuriytkach.batb.dr.DonatorsFilterMapper;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DonatorsStatsService {

  private final DonationStatsNotificationService notificationService;

  public void processStats(
    final DonatorsFilterMapper.DonatorSets finalDonators,
    final LocalDate startDate,
    final LocalDate endDate,
    final boolean readOnlyNotification
  ) {
    final long sum = StreamEx.of(finalDonators.grouped())
      .mapToLong(Donator::amount)
      .sum();
    log.info("Total amount donated for period {}-{}: {}", startDate, endDate, sum);

    final List<Long> amounts = finalDonators.ungrouped().stream()
      .map(Donator::amount)
      .sorted()
      .toList();

    final var amountStats = statsForListOfLongs(startDate, endDate, amounts, "amount", sum);

    final List<Long> counts = finalDonators.grouped().stream()
      .filter(not(Donator::isAnonymous))
      .map(Donator::count)
      .map(Integer::longValue)
      .sorted()
      .toList();
    log.info("Total number of named donations for period {}-{}: {}", startDate, endDate, counts.size());

    final var countStats = statsForListOfLongs(startDate, endDate, counts, "count", counts.size());

    notificationService.notifyForStats(
      amountStats,
      countStats,
      finalDonators.ungrouped().size(),
      startDate.getMonth(),
      readOnlyNotification
    );
  }

  private DonationStats statsForListOfLongs(
    final LocalDate startDate,
    final LocalDate endDate,
    final List<Long> amounts,
    final String statName,
    final long total
  ) {
    Optional<Long> maxAmount = amounts.stream().max(Long::compare);
    Optional<Long> minAmount = amounts.stream().min(Long::compare);
    double averageAmount = amounts.stream().mapToLong(Long::longValue).average().orElse(0.0);
    double medianAmount = getPercentile(amounts, 50);
    double p10Amount = getPercentile(amounts, 10);
    double p90Amount = getPercentile(amounts, 90);

    log.info(
      "Donation {} stats for period {}-{}: max={}, min={}, avg={}, median={}, p90={}, p10={}",
      statName,
      startDate,
      endDate,
      maxAmount.orElse(0L), minAmount.orElse(0L), averageAmount, medianAmount, p90Amount,
      p10Amount
    );

    return new DonationStats(
      total,
      maxAmount.orElse(0L),
      minAmount.orElse(0L),
      averageAmount,
      medianAmount,
      p90Amount,
      p10Amount
    );
  }

  private double getPercentile(List<Long> sortedList, double percentile) {
    if (sortedList.isEmpty()) {
      return 0;
    }
    int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
    return sortedList.get(index);
  }

}
