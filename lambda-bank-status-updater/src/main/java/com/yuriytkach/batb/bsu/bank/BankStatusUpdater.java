package com.yuriytkach.batb.bsu.bank;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.BankType;

import one.util.streamex.StreamEx;

public interface BankStatusUpdater {

  BankType bankType();

  Set<BankAccountStatus> updateAllAccountStatuses(Map<String, BankAccountStatus> previousStatuses);

  Set<BankAccountStatus> updateSpecifiedAccountStatuses(Map<String, BankAccountStatus> specificStatuses);

  default Set<BankAccountStatus> findUpdatedStatuses(
    final Map<String, BankAccountStatus> oldAccountStatuses,
    final Set<BankAccountStatus> retrievedAccountStatuses
  ) {
    return StreamEx.of(retrievedAccountStatuses)
      .flatMap(retrievedAccountStatus -> {
        final var oldAccountStatus = oldAccountStatuses.get(retrievedAccountStatus.accountId());
        if (oldAccountStatus == null
          || oldAccountStatus.amount() != retrievedAccountStatus.amount()
          || !Objects.equals(oldAccountStatus.spentAmount(), retrievedAccountStatus.spentAmount())
        ) {
          return Stream.of(retrievedAccountStatus);
        } else {
          return Stream.empty();
        }
      })
      .toImmutableSet();
  }

  default Set<BankAccountStatus> previousStatusesWithUpdated(
    final Map<String, BankAccountStatus> previousStatuses,
    final Set<BankAccountStatus> updatedAccountStatuses
  ) {
    final var updatedById = StreamEx.of(updatedAccountStatuses)
      .mapToEntry(BankAccountStatus::accountId, Function.identity())
      .toImmutableMap();

    return StreamEx.of(previousStatuses.values())
      .filter(Predicate.not(acc -> updatedById.containsKey(acc.accountId())))
      .append(updatedAccountStatuses)
      .toImmutableSet();
  }

}
