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
