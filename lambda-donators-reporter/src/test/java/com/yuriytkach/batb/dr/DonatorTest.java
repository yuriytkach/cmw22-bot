package com.yuriytkach.batb.dr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DonatorTest {

  @ParameterizedTest
  @CsvSource({
    "Unknown, true",
    "Bandera, false"
  })
  void shouldReturnAnonymousIfNameMatches(final String name, final boolean expected) {
    Donator donator = new Donator(name, 100L, 1, null);
    assertThat(donator.isAnonymous()).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "Bandera, true",
    "Бандера, false"
  })
  void shouldReturnTrueIfNameMatchesEnglishNamePattern(final String name, final boolean expected) {
    Donator donator = new Donator(name, 100L, 1, null);
    assertThat(donator.isEnglishName()).isEqualTo(expected);
  }

}
