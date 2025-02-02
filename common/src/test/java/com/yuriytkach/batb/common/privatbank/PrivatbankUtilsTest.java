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

package com.yuriytkach.batb.common.privatbank;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PrivatbankUtilsTest {

  private final PrivatbankUtils tested = new PrivatbankUtils();

  @ParameterizedTest
  @CsvSource({
    "0.00, 0",
    "0.01, 1",
    "0.10, 10",
    "1.00, 100",
    "1.01, 101",
    "1, 100",
    "1.1, 110",
  })
  void parseAmountShouldReturnCorrectValue(final String amount, final long expected) {
    assertThat(tested.parseAmount(amount)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid", "10.abc", "abc"})
  void parseAmountShouldReturnZeroForInvalidInput(final String amount) {
    assertThat(tested.parseAmount(amount)).isEqualTo(0L);
  }

  @ParameterizedTest
  @CsvSource({
    "2022-01-01, '01-01-2022'",
    "2022-12-31, '31-12-2022'",
    "2023-02-28, '28-02-2023'"
  })
  void formatDateShouldReturnCorrectValue(final LocalDate date, final String expected) {
    assertThat(tested.formatDate(date)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "'01.01.2022 00:00:00', '2022-01-01T00:00:00Z'",
    "'31.12.2022 23:59:59', '2022-12-31T23:59:59Z'",
    "'28.02.2023 12:34:56', '2023-02-28T12:34:56Z'"
  })
  void safeParseDateTimeShouldReturnCorrectValue(final String dateTime, final String expected) {
    assertThat(tested.safeParseDateTime(dateTime)).isEqualTo(Optional.of(Instant.parse(expected)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid", "01.abc.2022 00:00:00", "01.01.2022 abc"})
  void safeParseDateTimeShouldReturnEmptyForInvalidInput(final String dateTime) {
    assertThat(tested.safeParseDateTime(dateTime)).isEmpty();
  }
}
