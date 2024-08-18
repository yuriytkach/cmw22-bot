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

import java.util.Optional;
import java.util.stream.Stream;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@RegisterForReflection
public enum Currency {
  UAH(980, "₴"),
  USD(840, "$"),
  EUR(978, "€"),
  PLN(985, "zł"),
  GBP(826, "£"),
  CHF(756, "₣");

  private final int isoCode;
  private final String symbol;

  public static Optional<Currency> fromString(final String text) {
    return Stream.of(Currency.values())
      .filter(value -> value.name().equalsIgnoreCase(text))
      .findFirst();
  }

  public static Optional<Currency> fromIsoCode(final int isoCode) {
    return Stream.of(Currency.values())
      .filter(value -> value.getIsoCode() == isoCode)
      .findFirst();
  }
}
