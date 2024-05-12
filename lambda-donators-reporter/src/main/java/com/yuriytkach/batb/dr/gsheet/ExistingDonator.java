package com.yuriytkach.batb.dr.gsheet;

import java.util.List;
import java.util.Optional;

record ExistingDonator(int rowIndex, List<Object> columnValues) {

  String name() {
    return columnValues.getFirst().toString();
  }

  Optional<String> invertName() {
    final var parts = name().split(" ");
    if (parts.length == 2) {
      return Optional.of(parts[1] + " " + parts[0]);
    } else {
      return Optional.empty();
    }
  }
}
