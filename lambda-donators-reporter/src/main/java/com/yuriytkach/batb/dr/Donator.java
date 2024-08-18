package com.yuriytkach.batb.dr;

import static com.yuriytkach.batb.dr.tx.DonationTransaction.UNKNOWN;

import java.time.Instant;

public record Donator(String name, Long amount, int count, Instant lastTxDateTime) {

  public boolean isAnonymous() {
    return UNKNOWN.equals(name);
  }

  public boolean isEnglishName() {
    return name.matches("\\w+.*");
  }
}
