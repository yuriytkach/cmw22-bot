package com.yuriytkach.batb.dr.tx.mono.token;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class MonoTxNameMapper {

  private static final Pattern NAME_PATTERN = Pattern.compile(
    "від: (?<fname>\\w+|[^\\p{IsLatin}]+)\\s(?<sname>\\w+|[^\\p{IsLatin}]+)"
  );

  Optional<String> mapDonatorName(final String description) {
    if (description == null || description.isBlank()) {
      return Optional.empty();
    }

    final var matcher = NAME_PATTERN.matcher(description.toLowerCase(Locale.getDefault()));
    if (matcher.matches()) {
      log.trace("Matched donator name in {}", description);
      return Optional.of(
        StringUtils.capitalize(matcher.group("sname").toLowerCase(Locale.getDefault()))
          + " "
          + StringUtils.capitalize(matcher.group("fname").toLowerCase(Locale.getDefault()))
      );
    }
    return Optional.empty();
  }
}
