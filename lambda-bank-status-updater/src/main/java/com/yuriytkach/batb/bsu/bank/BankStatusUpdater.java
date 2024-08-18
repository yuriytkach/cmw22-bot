/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
