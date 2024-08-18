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

package com.yuriytkach.batb.dr.gsheet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.yuriytkach.batb.common.google.SheetUtilities;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
class GSheetColumnCalculator {

  private final SheetUtilities sheetUtilities;

  // Function to calculate the Excel column ID based on the starting column and a target date
  String calculateColumnId(final String startColumn, final LocalDate startDate, final LocalDate endDate) {
    final int startColumnIndex = sheetUtilities.columnToIndex(startColumn);
    final long monthsBetween = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), endDate.withDayOfMonth(1));
    final int targetColumnIndex = startColumnIndex + (int) monthsBetween;
    return sheetUtilities.indexToColumn(targetColumnIndex);
  }

}
