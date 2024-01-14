package com.yuriytkach.batb.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BankAccountStatusTest {

  @Test
  void shouldReturnShortAccountId() {
    final var bankAccountStatus = BankAccountStatus.builder()
      .accountId("1234567890123456")
      .build();

    assertThat(bankAccountStatus.shortAccountId()).isEqualTo("1234-56");
  }

}
