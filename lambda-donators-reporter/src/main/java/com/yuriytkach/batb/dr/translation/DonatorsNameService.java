package com.yuriytkach.batb.dr.translation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.dr.Donator;
import com.yuriytkach.batb.dr.translation.lambda.LambdaAiServiceRequest;
import com.yuriytkach.batb.dr.translation.lambda.LambdaInvoker;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DonatorsNameService {

  private final ObjectMapper objectMapper;
  private final LambdaInvoker lambdaInvoker;

  public Collection<Donator> translateEnglishNames(final Set<Donator> donatorsWithAmounts) {
    final List<String> englishNames = StreamEx.of(donatorsWithAmounts)
      .map(Donator::name)
      .filter(name -> name.matches("\\w+.*"))
      .distinct()
      .toImmutableList();
    log.info("English names found: {}", englishNames.size());
    log.debug("English names: {}", englishNames);

    if (englishNames.isEmpty()) {
      return donatorsWithAmounts;
    }

    return createLambdaPayload(englishNames)
      .map(lambdaInvoker::invokeLambda)
      .map(lambdaResponse -> mapTranslations(englishNames, lambdaResponse))
      .map(translations -> mapNames(donatorsWithAmounts, translations))
      .orElse(donatorsWithAmounts);
  }

  private Map<String, String> mapTranslations(final List<String> englishNames, final String lambdaResponse) {
    if (lambdaResponse.isBlank() || lambdaResponse.length() < 4) {
      log.warn("No translations were received from Lambda");
      return StreamEx.of(englishNames).mapToEntry(Function.identity()).distinctKeys().toImmutableMap();
    }
    final String[] translations = lambdaResponse.substring(1, lambdaResponse.length()-1).split(",");
    if (translations.length != englishNames.size()) {
      log.error("Translations count mismatch: {} != {}", translations.length, englishNames.size());
      return Map.of();
    }
    log.info("Zipping English names with translations: {}", englishNames.size());
    return StreamEx.of(englishNames)
      .zipWith(Arrays.stream(translations).map(String::strip))
      .toImmutableMap();
  }

  private Collection<Donator> mapNames(final Set<Donator> donators, final Map<String, String> translations) {
    if (translations.isEmpty()) {
      log.debug("No translations for names were found");
      return donators;
    }

    return StreamEx.of(donators)
      .map(donator -> new Donator(
        translations.getOrDefault(donator.name(), donator.name()),
        donator.amount(),
        donator.count(),
        donator.lastTxDateTime()
      ))
      .toImmutableList();
  }


  private Optional<String> createLambdaPayload(final List<String> englishNames) {
    final String payload = StreamEx.of(englishNames).joining(",");
    try {
      return Optional.of(objectMapper.writeValueAsString(
        new LambdaAiServiceRequest("translateNames", payload)
      ));
    } catch (final Exception ex) {
      log.error("Failed to serialize Lambda payload: {}", ex.getMessage());
      return Optional.empty();
    }
  }

}
