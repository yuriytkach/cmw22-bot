package com.yuriytkach.batb.dr.tx.privat;

import static java.util.regex.Pattern.compile;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.yuriytkach.batb.dr.tx.privat.api.Transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class PrivatTxNameMapper {

  private final Instance<NameResolver> nameResolvers;

  Optional<String> mapDonatorName(final Transaction tx) {
    return nameResolvers.stream()
      .map(resolver -> resolver.resolveName(tx))
      .flatMap(Optional::stream)
      .findFirst();
  }

  interface NameResolver {
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
  static class DescriptionNameResolver implements NameResolver {
    private static final Set<Pattern> PATTERNS = Set.of(
      compile(".*\\sвнески.*, (?<sname>[^\\p{IsLatin},]+)\\s(?<fname>[^\\p{IsLatin}]+)\\s[^\\p{IsLatin}]+"),
      compile("Благодійний внесок.*, (?<sname>[^\\p{IsLatin},]+)\\s(?<fname>[^\\p{IsLatin}]+)\\s[^\\p{IsLatin}]+"),
      compile("Сплата.*, (?<sname>[^\\p{IsLatin},]+)\\s(?<fname>[^\\p{IsLatin}]+)\\s[^\\p{IsLatin}]+"),
      compile(".*благодійність, (?<sname>[^\\p{IsLatin}]+)\\s(?<fname>[^\\p{IsLatin}]+)\\s[^\\p{IsLatin}]+"),
      compile("\\d{4}.*Послуги: (?<sname>\\w+),?\\s(?<fname>\\w+).*"),
      compile("\\d{4}.*Перекази: вiд (?<sname>\\w+)\\s(?<fname>\\w+).*"),
      compile("\\d{4}.*Зарахування переказу: (?<sname>\\w+)\\s(?<fname>\\w+).*")
    );

    @Override
    public Optional<String> resolveName(final Transaction tx) {
      return PATTERNS.stream()
        .map(pattern -> resolveNameFromPattern(pattern, tx.description()))
        .flatMap(Optional::stream)
        .findFirst();
    }
  }

  @ApplicationScoped
  static class ContrAgentNameResolver implements NameResolver {
    private static final Pattern PATTERN_1 = compile(
      "(?<sname>[^\\p{IsLatin}]+)\\s(?<fname>[^\\p{IsLatin}]+)\\s[^\\p{IsLatin}]+");

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
