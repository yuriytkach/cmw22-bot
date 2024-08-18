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

package com.yuriytkach.batb.dr.tx.mono.token;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class MonoTokenPeriodDatesAdjuster {

  public List<PeriodDates> adjust(final LocalDate startDate, final LocalDate endDate) {
    final long between = ChronoUnit.DAYS.between(startDate, endDate);
    if (between > 31) {
      log.info("Adjusting period dates for startDate: {} and endDate: {}", startDate, endDate);
      final var result = startDate.datesUntil(endDate, Period.ofMonths(1))
        .map(date -> new PeriodDates(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth())))
        .toList();
      log.info("Adjusted period dates: {}", result);
      return result;
    }

    return List.of(new PeriodDates(startDate, endDate));
  }

  record PeriodDates(LocalDate startDate, LocalDate endDate) { }
}
