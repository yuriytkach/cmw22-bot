package com.yuriytkach.batb.bsu.bank.privat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PrivatBalanceAmountParserTest {

  private final PrivatBalanceAmountParser tested = new PrivatBalanceAmountParser();

  @ParameterizedTest
  @CsvSource({
    "0.00, 0",
    "0.01, 1",
    "0.10, 10",
    "1.00, 100",
    "1.01, 101",
    "1, 100",
    "1.1, 110",
  })
  void shouldParseValid(final String amount, final long expected) {
    assertThat(tested.parseAmount(amount)).isEqualTo(expected);
  }
}
