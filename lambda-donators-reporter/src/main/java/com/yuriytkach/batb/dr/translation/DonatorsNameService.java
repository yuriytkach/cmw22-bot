package com.yuriytkach.batb.dr.translation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.dr.translation.lambda.LambdaAiServiceRequest;
import com.yuriytkach.batb.dr.translation.lambda.LambdaInvoker;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DonatorsNameService {

  private final ObjectMapper objectMapper;
  private final LambdaInvoker lambdaInvoker;

  public Map<String, Long> translateEnglishNames(final Map<String, Long> donatorsWithAmounts) {
    final List<String> englishNames = StreamEx.of(donatorsWithAmounts.keySet())
      .filter(name -> name.matches("\\w+.*"))
      .distinct()
      .toImmutableList();
    log.info("English names found: {}", englishNames.size());

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

  private Map<String, Long> mapNames(
    final Map<String, Long> donatorsWithAmounts,
    final Map<String, String> translations
  ) {
    if (translations.isEmpty()) {
      log.debug("No translations for names were found");
      return donatorsWithAmounts;
    }

    return EntryStream.of(donatorsWithAmounts)
      .mapKeys(name -> translations.getOrDefault(name, name))
      .grouping(Collectors.summingLong(Long::valueOf));
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
