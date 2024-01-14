package com.yuriytkach.batb.bsu.bank.privat;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class PrivatBalanceAmountParser {

  long parseAmount(final String amount) {
    try {
      final var num = Double.parseDouble(amount);
      return (long) (num * 100);
    } catch (final NumberFormatException ex) {
      log.error("Failed to parse amount {}: {}", amount, ex.getMessage());
      return 0L;
    }
  }

}
