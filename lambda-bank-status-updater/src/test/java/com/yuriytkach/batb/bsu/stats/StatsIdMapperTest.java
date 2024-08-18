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

package com.yuriytkach.batb.bsu.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatsIdMapperTest {

  private final StatsIdMapper tested = new StatsIdMapper();

  @ParameterizedTest
  @CsvSource({
    "Автомобілів, cars",
    "Тепловізори, thermals",
    "Додатки, addons",
    "Дадатки, addons",
    "Рацій, radios",
    "Дрони, drones",
    "Одяг/Взуття, clothes",
    "Старлінки, starlinks",
    "Генератори, generators",
    "Медицина, medicine",
    "Інше, other",
    "Іншого, other",
    "Технічне Оснащення, tech",
    "Броня, armors",
    "Some other, Some other"
  })
  void shouldMap(final String name, final String expected) {
    assertThat(tested.mapNameToId(name)).isEqualTo(expected);
  }

}
