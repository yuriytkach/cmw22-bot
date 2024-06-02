package com.yuriytkach.batb.dr.tx.mono.token;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yuriytkach.batb.dr.DonatorsReporterProperties;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class MonoTxNameMapper {

  private static final Pattern NAME_PATTERN = Pattern.compile(
    "від: (?<fname>\\w{3,}|[^\\p{IsLatin}]{3,})\\s(?<sname>\\w{3,}|[^\\p{IsLatin}]{3,})"
  );

  private final DonatorsReporterProperties donatorsReporterProperties;

  Optional<String> mapDonatorName(final String description) {
    if (description == null || description.isBlank()) {
      return Optional.empty();
    }

    final var matcher = NAME_PATTERN.matcher(description.toLowerCase(Locale.getDefault()));
    if (matcher.matches()) {
      log.trace("Matched donator name in {}", description);
      final String sname = StringUtils.capitalize(matcher.group("sname").toLowerCase(Locale.getDefault()));
      final String fname = StringUtils.capitalize(matcher.group("fname").toLowerCase(Locale.getDefault()));

      if (donatorsReporterProperties.nameStopWords().contains(sname)
          || donatorsReporterProperties.nameStopWords().contains(fname)) {
        log.trace("Donator name is in stop words list: {} {}", sname, fname);
        return Optional.empty();
      }

      return Optional.of(sname + " " + fname);
    }
    return Optional.empty();
  }
}
