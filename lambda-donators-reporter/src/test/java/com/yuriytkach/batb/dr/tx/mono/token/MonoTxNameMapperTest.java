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

package com.yuriytkach.batb.dr.tx.mono.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuriytkach.batb.dr.DonatorsReporterProperties;

@ExtendWith(MockitoExtension.class)
class MonoTxNameMapperTest {

  @Mock
  private DonatorsReporterProperties donatorsReporterProperties;

  @InjectMocks
  private MonoTxNameMapper tested;

  @ParameterizedTest
  @CsvSource({
    "Від: Степан Бандера, Бандера Степан",
    "Від: STEPAN BANDERA, Bandera Stepan",
    "Від: 🐈, ",
    "Від: Степан, ",
    "Від: P24 CR MD UA, ",
    "Від: P Ihor, ",
    "Від: PP Ihor, ",
    "Від: PP Ihor, ",
    "Від: Hello Ihor, ",
    "Від: Ihor World, "
  })
  void shouldMapDescription(final String description, final String expectedName) {
    lenient().when(donatorsReporterProperties.nameStopWords()).thenReturn(Set.of("Hello", "World"));

    assertThat(tested.mapDonatorName(description)).isEqualTo(Optional.ofNullable(expectedName));
  }
}
