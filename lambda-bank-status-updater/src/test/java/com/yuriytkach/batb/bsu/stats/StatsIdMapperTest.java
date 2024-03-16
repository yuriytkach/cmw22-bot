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
    "Рацій, radios",
    "Дрони, drones",
    "Одяг/Взуття, clothes",
    "Старлінки, starlinks",
    "Генератори, generators",
    "Медицина, medicine",
    "Інше, other",
    "Технічне Оснащення, tech",
    "Броня, armors",
    "Some other, Some other"
  })
  void shouldMap(final String name, final String expected) {
    assertThat(tested.mapNameToId(name)).isEqualTo(expected);
  }

}
