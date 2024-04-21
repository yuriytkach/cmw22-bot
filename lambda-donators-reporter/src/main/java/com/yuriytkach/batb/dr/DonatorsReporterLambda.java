package com.yuriytkach.batb.dr;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.dr.tx.DonationTransactionFetcher;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named("donatorsReporter")
public class DonatorsReporterLambda implements RequestHandler<Object, Void> {

  @Inject
  Instance<DonationTransactionFetcher> txFetchers;

  @Override
  public Void handleRequest(final Object ignored, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Donators Reporter Lambda. AWS Request ID: {}", context.getAwsRequestId());

    return null;
  }
}
