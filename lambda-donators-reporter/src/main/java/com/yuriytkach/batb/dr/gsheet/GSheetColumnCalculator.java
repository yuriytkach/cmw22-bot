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
