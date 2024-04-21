package com.yuriytkach.batb.common.privatbank;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PrivatbankUtils {

  static final DateTimeFormatter START_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  public long parseAmount(final String amount) {
    try {
      final var num = Double.parseDouble(amount);
      return (long) (num * 100);
    } catch (final NumberFormatException ex) {
      log.error("Failed to parse amount {}: {}", amount, ex.getMessage());
      return 0L;
    }
  }

  public String formatDate(final LocalDate localDate) {
    return localDate.format(START_DATE_FORMATTER);
  }

  public Optional<Instant> safeParseDateTime(final String dateTime) {
    try {
      final TemporalAccessor parsed = DATE_TIME_FORMATTER.parse(dateTime);
      return Optional.of(LocalDateTime.from(parsed).toInstant(ZoneOffset.UTC));
    } catch (final Exception ex) {
      log.warn("Cannot parse privatbank datetime: {} {}", dateTime, ex.getMessage());
      return Optional.empty();
    }
  }


}
