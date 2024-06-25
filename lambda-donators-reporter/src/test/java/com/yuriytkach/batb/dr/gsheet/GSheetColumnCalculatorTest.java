package com.yuriytkach.batb.dr.gsheet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.yuriytkach.batb.common.google.SheetUtilities;

class GSheetColumnCalculatorTest {

  private final GSheetColumnCalculator tested = new GSheetColumnCalculator(new SheetUtilities());

  @ParameterizedTest
  @CsvSource({
    "A, 2023-01-01, 2023-01-01, A",  // No change
    "A, 2023-01-01, 2023-02-01, B",  // Simple increment
    "Z, 2023-01-01, 2023-02-01, AA", // Transition from single to double letters
    "AA, 2023-01-01, 2023-01-01, AA",// No change in double letters
    "AZ, 2023-01-01, 2023-02-01, BA",// End of double letter cycle
    "D, 2023-01-01, 2023-12-01, O",  // Several months forward within the same year
    "D, 2023-01-01, 2024-08-01, W",  // Several months forward with next year
    "D, 2023-01-01, 2025-01-01, AB",  // Two years later, same month
    "B, 2023-05-01, 2023-04-01, A",  // Backward one month (reverse case)
    "Z, 2023-01-01, 2024-02-01, AM", // Over a year from Z
    "G, 2023-01-01, 2043-01-01, IM"  // Large span of 20 years
  })

  void shouldCalculateColIndex(
    final String startColumn, final LocalDate startDate, final LocalDate endDate, final String expectedColumn
  ) {
    final var actual = tested.calculateColumnId(startColumn, startDate, endDate);
    assertThat(actual).isEqualTo(expectedColumn);
  }
}
