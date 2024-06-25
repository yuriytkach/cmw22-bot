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
