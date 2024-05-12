package com.yuriytkach.batb.dr.gsheet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class GSheetDateColumnCalculator {

  // Function to calculate the Excel column ID based on the starting column and a target date
  String calculateColumnId(final String startColumn, final LocalDate startDate, final LocalDate endDate) {
    final int startColumnIndex = columnToIndex(startColumn);
    final long monthsBetween = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
    final int targetColumnIndex = startColumnIndex + (int) monthsBetween;
    return indexToColumn(targetColumnIndex);
  }

  // Helper method to convert a column string to its corresponding index number
  private int columnToIndex(final String column) {
    return IntStream.range(0, column.length())
      .reduce(0, (index, i) -> index * 26 + (column.charAt(i) - 'A' + 1));
  }

  // Helper method to convert an index number back to a column
  private String indexToColumn(final int index) {
    final StringBuilder column = new StringBuilder();
    IntStream.iterate(index, i -> i > 0, i -> (i - 1) / 26)
      .map(i -> (i - 1) % 26)
      .forEach(remainder -> column.insert(0, (char) ('A' + remainder)));
    return column.toString();
  }

}
