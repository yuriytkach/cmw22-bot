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
