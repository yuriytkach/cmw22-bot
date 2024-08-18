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
