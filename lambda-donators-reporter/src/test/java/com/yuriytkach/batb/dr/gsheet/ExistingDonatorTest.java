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
    assertThat(new ExistingDonator(1, List.of("John Doe", "42")).invertName()).isEqualTo("Doe John");
  }

}
