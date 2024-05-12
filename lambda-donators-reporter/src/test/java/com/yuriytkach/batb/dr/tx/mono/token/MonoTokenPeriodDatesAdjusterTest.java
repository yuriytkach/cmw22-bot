package com.yuriytkach.batb.dr.tx.mono.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.yuriytkach.batb.dr.tx.mono.token.MonoTokenPeriodDatesAdjuster.PeriodDates;

class MonoTokenPeriodDatesAdjusterTest {

  private final MonoTokenPeriodDatesAdjuster tested = new MonoTokenPeriodDatesAdjuster();

  @Test
  void shouldNotAdjustIfLessThan31Days() {
    assertThat(tested.adjust(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
      .containsExactly(new PeriodDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)));
  }

  @Test
  void shouldAdjustForTwoMonths() {
    assertThat(tested.adjust(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 28)))
      .containsExactly(
        new PeriodDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)),
        new PeriodDates(LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28))
      );
  }

  @Test
  void shouldAdjustForOneYear() {
    assertThat(tested.adjust(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31)))
      .containsExactly(
        new PeriodDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)),
        new PeriodDates(LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28)),
        new PeriodDates(LocalDate.of(2023, 3, 1), LocalDate.of(2023, 3, 31)),
        new PeriodDates(LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 30)),
        new PeriodDates(LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 31)),
        new PeriodDates(LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30)),
        new PeriodDates(LocalDate.of(2023, 7, 1), LocalDate.of(2023, 7, 31)),
        new PeriodDates(LocalDate.of(2023, 8, 1), LocalDate.of(2023, 8, 31)),
        new PeriodDates(LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 30)),
        new PeriodDates(LocalDate.of(2023, 10, 1), LocalDate.of(2023, 10, 31)),
        new PeriodDates(LocalDate.of(2023, 11, 1), LocalDate.of(2023, 11, 30)),
        new PeriodDates(LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 31))
      );
  }

}
