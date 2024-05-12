package com.yuriytkach.batb.dr;

import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.dr.translation.DonatorsNameService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
class DonatorsFilterMapper {

  @Inject
  DonatorsReporterProperties properties;

  @Inject
  DonatorsNameService donatorsNameService;

  @Inject
  SecretsReader secretsReader;

  Set<Donator> mapAndGroupDonators(final Collection<Donator> donatorsPerTransaction) {
    log.debug("Donators before mapping/filtering: {}", donatorsPerTransaction.size());

    final var uniqueDonators = groupDonatorsByName(donatorsPerTransaction);
    log.debug("Donators after grouping by name: {}", uniqueDonators.size());

    final var translatedDonators = donatorsNameService.translateEnglishNames(uniqueDonators);
    final var uniqueTranslatedDonators = groupDonatorsByName(translatedDonators);
    log.debug("Donators after translating names: {}", uniqueTranslatedDonators.size());

    return secretsReader.readSecret(properties.ignoredDonatorsKey())
      .map(ignoredNamesString -> filterDonatorsByName(uniqueTranslatedDonators, ignoredNamesString))
      .orElse(uniqueTranslatedDonators);
  }

  Set<Donator> filterDonatorsByAmount(final Set<Donator> groupedDonators) {
    final var filteredByAmount = StreamEx.of(groupedDonators)
      .filter(donator -> donator.amount() >= properties.minAmountCents())
      .toImmutableSet();
    log.debug("Donators with amounts >{} : {}", properties.minAmountCents() / 100, filteredByAmount.size());
    return filteredByAmount;
  }

  private Set<Donator> groupDonatorsByName(final Collection<Donator> donators) {
    return StreamEx.of(StreamEx.of(donators).groupingBy(Donator::name).values())
      .map(group -> new Donator(group.getFirst().name(), group.stream().mapToLong(Donator::amount).sum(), group.size()))
      .toImmutableSet();
  }

  private Set<Donator> filterDonatorsByName(
    final Set<Donator> donators,
    final String ignoredNamesString
  ) {
    final var ignoredNames = Set.copyOf(List.of(ignoredNamesString.split(",")));
    log.info("Configured ignored donators names: {}", ignoredNames.size());

    final var filteredDonators = StreamEx.of(donators)
      .filter(not(donator -> ignoredNames.contains(donator.name())))
      .toImmutableSet();
    log.debug("Donators after filtering by ignored names: {}", filteredDonators.size());
    return filteredDonators;
  }

}
