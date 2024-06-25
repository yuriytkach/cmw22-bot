package com.yuriytkach.batb.bc.check;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.yuriytkach.batb.bc.Person;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BirthdayFinder {

  private final Clock clock;

  public Optional<Person> findBirthday(final List<Person> people, final String overrideName) {
    final LocalDate now = LocalDate.now(clock);
    log.info("Current date: {}", now);
    log.debug("Checking birthday among {} people", people.size());

    return people.stream()
      .filter(person -> filterPerson(person, now, overrideName))
      .findFirst();
  }

  private boolean filterPerson(final Person person, final LocalDate now, final String overrideName) {
    if (overrideName != null && person.name().startsWith(overrideName)) {
      return true;
    }
    return person.birthday().getDayOfMonth() == now.getDayOfMonth()
      && person.birthday().getMonth() == now.getMonth();
  }
}
