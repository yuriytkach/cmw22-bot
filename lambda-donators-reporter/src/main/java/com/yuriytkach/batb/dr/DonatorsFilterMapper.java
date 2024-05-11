package com.yuriytkach.batb.dr;

import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.dr.translation.DonatorsNameService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;

@Slf4j
@ApplicationScoped
class DonatorsFilterMapper {

  @Inject
  DonatorsReporterProperties properties;

  @Inject
  DonatorsNameService donatorsNameService;

  @Inject
  SecretsReader secretsReader;

  Map<String, Long> filterAndMapDonators(final Map<String, Long> donatorsWithAmounts) {
    log.debug("Donators before mapping/filtering: {}", donatorsWithAmounts.size());

    final var filteredByAmount = EntryStream.of(donatorsWithAmounts)
      .filterValues(amount -> amount >= properties.minAmountCents())
      .toImmutableMap();
    log.debug("Donators with amounts >{} : {}", properties.minAmountCents() / 100, filteredByAmount.size());

    final var translatedNames = donatorsNameService.translateEnglishNames(filteredByAmount);
    log.debug("Donators after translating names: {}", translatedNames.size());

    return secretsReader.readSecret(properties.ignoredDonatorsKey())
      .map(ignoredNamesString -> filterDonatorsByName(translatedNames, ignoredNamesString))
      .orElse(translatedNames);
  }

  private Map<String, Long> filterDonatorsByName(
    final Map<String, Long> translatedNames,
    final String ignoredNamesString
  ) {
    final var ignoredNames = Set.copyOf(List.of(ignoredNamesString.split(",")));
    log.info("Configured ignored donators names: {}", ignoredNames.size());

    final var filteredDonators = EntryStream.of(translatedNames)
      .filterKeys(not(ignoredNames::contains))
      .toImmutableMap();
    log.debug("Donators after filtering by ignored names: {}", filteredDonators.size());
    return filteredDonators;
  }

}
