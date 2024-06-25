package com.yuriytkach.batb.bc.check;

import java.util.Optional;

import com.yuriytkach.batb.bc.Person;
import com.yuriytkach.batb.common.google.SheetsFactory;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class BirthdayCheckService {

  @Inject
  SheetsFactory sheetsFactory;

  @Inject
  BankAccessStorage bankAccessStorage;

  @Inject
  GSheetService gSheetService;

  @Inject
  BirthdayFinder birthdayFinder;

  public Optional<Person> checkAndFindBirthdayPerson(final String overrideName) {
    return bankAccessStorage.getRegistryConfig().flatMap(
      registryConfig -> sheetsFactory.getSheetsService()
        .map(sheets -> gSheetService.readPeople(registryConfig, sheets))
        .flatMap(people -> birthdayFinder.findBirthday(people, overrideName))
    );
  }
}
