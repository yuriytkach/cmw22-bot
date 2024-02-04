package com.yuriytkach.batb.common;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record BankAccountStatus (
  BankType bankType,
  String accountId,
  String accountName,
  long amount,

  long amountUah,

  Currency currency,
  Instant updatedAt,

  Boolean ignoreForTotal
) {

  public String shortAccountId() {
    return accountId.substring(0, 4) + "-" + accountId.substring(accountId.length() - 2);
  }

  public boolean isIgnoreForTotal() {
    return ignoreForTotal != null && ignoreForTotal;
  }

}
