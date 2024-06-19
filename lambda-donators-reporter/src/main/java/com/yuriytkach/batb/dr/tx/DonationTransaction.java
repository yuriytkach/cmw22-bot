package com.yuriytkach.batb.dr.tx;

import java.time.Instant;

import lombok.Builder;

@Builder
public record DonationTransaction(
  String id,
  String name,
  long amountUah,
  Instant date
) {

  public static final String UNKNOWN = "Unknown";

}
