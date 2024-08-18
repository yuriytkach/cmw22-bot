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

package com.yuriytkach.batb.common.google;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SheetUtilitiesTest {

  private final SheetUtilities tested = new SheetUtilities();

  @ParameterizedTest
  @CsvSource({
    "A, 0",
    "B, 1",
    "Z, 25",
    "AA, 26",
    "AB, 27",
    "AZ, 51",
    "BA, 52",
    "IM, 246"
  })
  void shouldConvertColumnToIndex(final String col, final int expectedIndex) {
    final int actualIndex = tested.columnToIndex(col);
    assertThat(actualIndex).isEqualTo(expectedIndex);
  }

  @ParameterizedTest
  @CsvSource({
    "0, A",
    "1, B",
    "25, Z",
    "26, AA",
    "27, AB",
    "51, AZ",
    "52, BA",
    "246, IM"
  })
  void shouldConvertIndexToColumn(final int index, final String expectedCol) {
    final String actualCol = tested.indexToColumn(index);
    assertThat(actualCol).isEqualTo(expectedCol);
  }
}
