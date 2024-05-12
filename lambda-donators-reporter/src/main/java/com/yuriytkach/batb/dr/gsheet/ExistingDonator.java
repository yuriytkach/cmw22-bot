package com.yuriytkach.batb.dr.gsheet;

import java.util.List;

record ExistingDonator(int rowIndex, List<Object> columnValues) {

  String name() {
    return columnValues.getFirst().toString();
  }
}
