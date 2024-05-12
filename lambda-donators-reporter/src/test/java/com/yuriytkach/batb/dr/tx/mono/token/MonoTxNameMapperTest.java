package com.yuriytkach.batb.dr.tx.mono.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MonoTxNameMapperTest {

  private final MonoTxNameMapper tested = new MonoTxNameMapper();

  @ParameterizedTest
  @CsvSource({
    "Від: Степан Бандера, Бандера Степан",
    "Від: STEPAN BANDERA, Bandera Stepan",
    "Від: 🐈, ",
    "Від: Степан, ",
    "Від: P24 CR MD UA, "
  })
  void shouldMapDescription(final String description, final String expectedName) {
    assertThat(tested.mapDonatorName(description)).isEqualTo(Optional.ofNullable(expectedName));
  }
}
