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
  Currency currency,
  Instant updatedAt
) {

  public String shortAccountId() {
    return accountId.substring(0, 4) + "-" + accountId.substring(accountId.length() - 2);
  }

}
