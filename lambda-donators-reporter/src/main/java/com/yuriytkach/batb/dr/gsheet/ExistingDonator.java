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
