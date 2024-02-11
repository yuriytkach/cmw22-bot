package com.yuriytkach.batb.bsu;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.common.StatusUpdateRequest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named("bankStatusUpdater")
public class BankStatusUpdaterLambda implements RequestHandler<StatusUpdateRequest, Void> {

  @Inject
  CurrentFundraiserUpdater currentFundraiserUpdater;

  @Inject
  AllAccountsUpdater allAccountsUpdater;

  @Override
  @SneakyThrows
  public Void handleRequest(final StatusUpdateRequest input, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Bank Status Updater for Input {}. AWS Request ID: {}", input, context.getAwsRequestId());

    switch (input.check()) {
      case CURRENT -> currentFundraiserUpdater.update();
      case FULL -> allAccountsUpdater.update();
    }

    return null;
  }
}
