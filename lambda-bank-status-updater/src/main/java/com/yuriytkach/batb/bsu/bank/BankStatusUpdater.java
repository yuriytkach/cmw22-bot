package com.yuriytkach.batb.bsu.bank;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.yuriytkach.batb.common.BankAccountStatus;

import one.util.streamex.StreamEx;

public interface BankStatusUpdater {

  void updateAccountStatuses(Map<String, BankAccountStatus> accountStatuses);

  default Set<BankAccountStatus> findUpdatedStatuses(
    final Map<String, BankAccountStatus> oldAccountStatuses,
    final Set<BankAccountStatus> retrievedAccountStatuses
  ) {
    return StreamEx.of(retrievedAccountStatuses)
      .flatMap(retrievedAccountStatus -> {
        final var oldAccountStatus = oldAccountStatuses.get(retrievedAccountStatus.accountId());
        if (oldAccountStatus == null || oldAccountStatus.amount() != retrievedAccountStatus.amount()) {
          return Stream.of(retrievedAccountStatus);
        } else {
          return Stream.empty();
        }
      })
      .toImmutableSet();
  }

}
