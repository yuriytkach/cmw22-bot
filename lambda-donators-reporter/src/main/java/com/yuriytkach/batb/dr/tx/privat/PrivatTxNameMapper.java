package com.yuriytkach.batb.dr.tx.privat;

import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yuriytkach.batb.dr.DonatorsReporterProperties;
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
        log.trace(
          "Matched donator name with `{}`\n{}\n{}",
          this.getClass().getSimpleName(),
          pattern,
          value
        );
        return Optional.of(
          StringUtils.capitalize(matcher.group("sname").toLowerCase(Locale.getDefault()))
            + " "
            + StringUtils.capitalize(matcher.group("fname").toLowerCase(Locale.getDefault()))
        );
      }
      return Optional.empty();
    }
  }

  @ApplicationScoped
  @RequiredArgsConstructor
  static final class DescriptionNameResolver implements PrivatNameResolver {

    private static final Set<Pattern> PATTERNS = Set.of(
      compile(".*\\sвнески.*, (?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("благо.* внес.*, (?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("(.плата|збір|допомога|пожертва).*, "
        + "(?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("на.*(зсу|бпла|дрон|рації|перемог.).*, "
        + "(?<sname>[^\\p{IsLatin},]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile(".*благодійність, (?<sname>[^\\p{IsLatin}]{3,})\\s(?<fname>[^\\p{IsLatin}]{3,})\\s[^\\p{IsLatin}]+"),
      compile("\\d{4}.*послуги: (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*інше: від (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*iнше: вiд (?<sname>\\w{3,}),?\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*перекази: вiд (?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*"),
      compile("\\d{4}.*арахування переказу: (вiд )?(?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*"),
      compile(".арахування переказу: (вiд )?(?<sname>\\w{3,})\\s(?<fname>\\w{3,}).*")
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
      if ("БФ СПІВДРУЖНІСТЬ-22 ТОВ".equals(tx.contrAgentName())) {
        return Optional.empty();
      }

      return Optional.ofNullable(tx.contrAgentName())
        .filter(value -> !value.isBlank()
          && !value.startsWith("CH_")
          && !value.startsWith("P2P")
          && !value.contains("СПІВДРУЖНІСТЬ-22")
          && !value.matches("\\d+.*")
        )
        .flatMap(value -> resolveNameFromPattern(PATTERN_1, value))
        .or(() -> Optional.ofNullable(tx.contrAgentName())
          .filter(value -> value.matches("^ТОВ.+|.+ТОВ$"))
        );
    }
  }
}
