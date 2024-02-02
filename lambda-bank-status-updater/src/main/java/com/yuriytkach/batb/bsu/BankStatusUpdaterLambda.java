package com.yuriytkach.batb.bsu;

import java.util.function.Function;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.bsu.bank.BankStatusUpdater;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Named("bankStatusUpdater")
public class BankStatusUpdaterLambda implements RequestHandler<Void, Void> {

  @Inject
  Instance<BankStatusUpdater> updaters;

  @Inject
  AccountBalanceStorage accountBalanceStorage;

  @Override
  public Void handleRequest(final Void input, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Bank Status Updater. AWS Request ID: {}", context.getAwsRequestId());

    final var accountStatuses = StreamEx.of(accountBalanceStorage.getAll())
      .mapToEntry(BankAccountStatus::accountId, Function.identity())
      .toImmutableMap();
    log.debug("Loaded account statuses: {}", accountStatuses.size());

    log.info("Updating bank statuses with updaters: {}", updaters.stream().count());

    updaters.stream().forEach(updated -> updated.updateAccountStatuses(accountStatuses));

    return null;
  }
}
