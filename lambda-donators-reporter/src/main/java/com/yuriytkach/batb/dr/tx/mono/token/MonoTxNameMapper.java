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
