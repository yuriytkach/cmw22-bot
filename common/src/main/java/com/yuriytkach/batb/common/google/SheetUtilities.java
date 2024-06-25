package com.yuriytkach.batb.common.google;

import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SheetUtilities {

  /**
   * Helper method to convert a column string to its corresponding index number
   */
  public int columnToIndex(final String column) {
    final var oneBased = IntStream.range(0, column.length())
      .reduce(0, (index, i) -> index * 26 + (column.charAt(i) - 'A' + 1));
    return oneBased - 1;
  }

  /**
   * Helper method to convert an index number back to a column
   */
  public String indexToColumn(final int index) {
    final StringBuilder column = new StringBuilder();
    IntStream.iterate(index + 1, i -> i > 0, i -> (i - 1) / 26)
      .map(i -> (i - 1) % 26)
      .forEach(remainder -> column.insert(0, (char) ('A' + remainder)));
    return column.toString();
  }

}
