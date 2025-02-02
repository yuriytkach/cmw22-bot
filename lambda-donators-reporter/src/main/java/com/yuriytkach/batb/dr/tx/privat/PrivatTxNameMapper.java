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

package com.yuriytkach.batb.dr.tx.privat;

import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yuriytkach.batb.dr.DonatorsReporterProperties;
import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.privat.api.Transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class PrivatTxNameMapper {

  private final Instance<PrivatNameResolver> nameResolvers;

  Optional<String> mapDonatorName(final Transaction tx) {
    return nameResolvers.stream()
      .map(resolver -> resolver.resolveName(tx))
      .flatMap(Optional::stream)
      .findFirst();
  }

  interface PrivatNameResolver {
    Optional<String> resolveName(final Transaction tx);

    default Optional<String> resolveNameFromPattern(final Pattern pattern, final String value) {
      final var matcher = pattern.matcher(value);
      if (matcher.matches()) {
        final var namedGroups = matcher.namedGroups();
        final var fnameGroup = namedGroups.containsKey("fname1") && matcher.group("fname1") != null
                               ? matcher.group("fname1") : matcher.group("fname");
        final var snameGroup = namedGroups.containsKey("sname1") && matcher.group("sname1") != null
                               ? matcher.group("sname1") : matcher.group("sname");

        log.trace(
          "Matched donator name with `{}`\n{}\n{}\n{} {}",
          this.getClass().getSimpleName(),
          pattern,
          value,
          fnameGroup,
          snameGroup
        );

        final String fname = StringUtils.capitalize(fnameGroup.toLowerCase(Locale.getDefault()));
        final String sname = StringUtils.capitalize(snameGroup.toLowerCase(Locale.getDefault()));

        return Optional.of(sname + " " + fname);
      }
      return Optional.empty();
    }
  }

  @ApplicationScoped
  @RequiredArgsConstructor
  static final class DescriptionNameResolver implements PrivatNameResolver {

    private static final List<Pattern> PATTERNS = List.of(
      compile(".лаго.*\\sвнес.*в.д\\s+(?<fname1>\\w{3,})\\s(?<sname1>\\w{3,}),\\s"
        + "(?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile(".*\\sвнес.*, (?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("(.плата|збір|допомога|пожертва).*, "
        + "(?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("на.*(зсу|бпла|дрон|рації|перемог.).*, "
        + "(?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile(".*благодійність, (?<sname>[^\\p{IsLatin}]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("\\d{4}.*послуги: (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*інше: в.д (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*iнше: в.д (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*перекази: в.д (?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*арахування переказу: (в.д )?(?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*"),
      compile(".арахування переказу: (в.д )?(?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*")
    );

    private final DonatorsReporterProperties donatorsReporterProperties;

    @Override
    public Optional<String> resolveName(final Transaction tx) {
      return PATTERNS.stream()
        .map(pattern -> resolveNameFromPattern(pattern, tx.description().toLowerCase(Locale.getDefault())))
        .flatMap(Optional::stream)
        .filter(not(name -> donatorsReporterProperties.nameStopWords().stream().anyMatch(name::contains)))
        .findFirst();
    }
  }

  @ApplicationScoped
  static final class ContrAgentNameResolver implements PrivatNameResolver {
    private static final Pattern PATTERN_1 = compile(
      "(?<sname>[^\\p{IsLatin}]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+");

    @Override
    public Optional<String> resolveName(final Transaction tx) {
      final String contrAgentName = tx.contrAgentName();

      if (contrAgentName != null && contrAgentName.contains("СПІВДРУЖНІСТЬ-22")) {
        return Optional.of(DonationTransaction.SELF_NAME);
      }

      return Optional.ofNullable(contrAgentName)
        .filter(value -> !value.isBlank()
          && !value.startsWith("CH_")
          && !value.startsWith("P2P")
          && !value.contains("СПІВДРУЖНІСТЬ-22")
          && !value.matches("\\d+.*")
        )
        .flatMap(value -> resolveNameFromPattern(PATTERN_1, value))
        .or(() -> Optional.ofNullable(contrAgentName)
          .filter(value -> value.matches("^ТОВ.+|.+ТОВ$"))
        );
    }
  }
}
