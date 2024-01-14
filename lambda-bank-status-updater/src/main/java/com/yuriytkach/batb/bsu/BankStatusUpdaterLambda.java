package com.yuriytkach.batb.bsu;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named("bankStatusUpdater")
public class BankStatusUpdaterLambda implements RequestHandler<Void, Void> {

  @Inject
  Instance<BankStatusUpdater> updaters;

  @Override
  public Void handleRequest(final Void input, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Bank Status Updater. AWS Request ID: {}", context.getAwsRequestId());

    log.info("Updating bank statuses with updaters: {}", updaters.stream().count());

    updaters.stream().forEach(BankStatusUpdater::updateAccountStatuses);

    return null;
  }
}
