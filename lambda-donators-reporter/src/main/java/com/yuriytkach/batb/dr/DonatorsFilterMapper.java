package com.yuriytkach.batb.dr;

import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.Comparator;
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
public class DonatorsFilterMapper {

  @Inject
  DonatorsReporterProperties properties;

  @Inject
  DonatorsNameService donatorsNameService;

  @Inject
  SecretsReader secretsReader;

  public Set<Donator> filterDonatorsByAmount(final Set<Donator> groupedDonators) {
    final var filtered = StreamEx.of(groupedDonators)
      .filter(donator -> donator.amount() >= properties.minAmountCents())
      .toImmutableSet();
    log.debug("Donators with amounts >{} : {}", properties.minAmountCents() / 100, filtered.size());
    return filtered;
  }

  public Set<Donator> filterDonatorsByCount(final Set<Donator> groupedDonators) {
    final var filtered = StreamEx.of(groupedDonators)
      .filter(donator -> donator.count() >= properties.minCount())
      .toImmutableSet();
    log.debug("Donators with count >{} : {}", properties.minCount(), filtered.size());
    return filtered;
  }

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

  private Set<Donator> groupDonatorsByName(final Collection<Donator> donators) {
    return StreamEx.of(StreamEx.of(donators).groupingBy(Donator::name).values())
      .map(group -> new Donator(
        group.getFirst().name(),
        group.stream().mapToLong(Donator::amount).sum(),
        group.stream().mapToInt(Donator::count).sum(),
        group.stream().map(Donator::lastTxDateTime).max(Comparator.naturalOrder()).orElseThrow()
      ))
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
