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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ExistingDonatorTest {

  @Test
  void shouldReturnName() {
    assertThat(new ExistingDonator(1, List.of("John Doe", "42")).name()).isEqualTo("John Doe");
  }

  @Test
  void shouldReturnInvertedName() {
    assertThat(new ExistingDonator(1, List.of("John Doe", "42")).invertName()).hasValue("Doe John");
  }

}
