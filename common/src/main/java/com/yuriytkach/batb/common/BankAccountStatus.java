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

package com.yuriytkach.batb.common;

import java.time.Instant;

import javax.annotation.Nullable;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder(toBuilder = true)
@RegisterForReflection
public record BankAccountStatus (
  BankType bankType,
  String accountId,
  String accountName,
  long amount,

  long amountUah,

  @Nullable
  Long spentAmount,

  @Nullable
  Long spentAmountUah,

  Currency currency,
  Instant updatedAt,

  Boolean ignoreForTotal,

  @Nullable
  String gsheetStatRow
) {

  public String shortAccountId() {
    return accountId.substring(0, 4) + "-" + accountId.substring(accountId.length() - 2);
  }

  public boolean isIgnoreForTotal() {
    return ignoreForTotal != null && ignoreForTotal;
  }

}
