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
