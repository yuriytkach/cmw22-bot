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

package com.yuriytkach.batb.bc;

import java.util.Optional;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.bc.check.BirthdayCheckService;
import com.yuriytkach.batb.bc.notify.BirthdayNotificationService;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BirthdayCheckLambda implements RequestHandler<BirthdayCheckLambdaRequest, Void> {

  @Inject
  BirthdayCheckService birthdayCheckService;

  @Inject
  BirthdayNotificationService birthdayNotificationService;

  @Override
  public Void handleRequest(final BirthdayCheckLambdaRequest request, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Request: {}. AWS Request ID: {}", request, context.getAwsRequestId());

    final Optional<Person> personOpt = birthdayCheckService.checkAndFindBirthdayPerson(request.overrideName());
    personOpt.ifPresentOrElse(
      person -> birthdayNotificationService.notifyAboutBirthday(person, request.readOnly()),
      () -> log.info("No person with birthday found")
    );
    return null;
  }
}
