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
