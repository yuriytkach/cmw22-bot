package com.yuriytkach.batb.dr;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.dr.gsheet.DonatorsTableUpdater;
import com.yuriytkach.batb.dr.tx.DonationTransactionFetcher;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Named("donatorsReporter")
public class DonatorsReporterLambda implements RequestHandler<DonatorsReporterLambdaRequest, Void> {

  @Inject
  Instance<DonationTransactionFetcher> txFetchers;

  @Inject
  DonatorsReporterProperties properties;

  @Inject
  DonatorsFilterMapper donatorsFilterMapper;

  @Inject
  DonatorsTableUpdater donatorsTableUpdater;

  @Inject
  Clock clock;

  @Override
  public Void handleRequest(final DonatorsReporterLambdaRequest request, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Request: {}. AWS Request ID: {}", request, context.getAwsRequestId());

    final LocalDate startDate = createStartOfMonthDate();
    final LocalDate endDate = createEndOfMonthDate();

    log.debug("Fetching transactions in date range: {} - {}", startDate, endDate);

    final Collection<Donator> donatorsWithAmounts = StreamEx.of(txFetchers.stream())
      .map(fetcher -> fetcher.fetchTransactions(startDate, endDate))
      .flatMap(Collection::stream)
      .map(tx -> new Donator(tx.name(), tx.amountUah(), 1, tx.date()))
      .toImmutableList();

    final Set<Donator> finalDonators = donatorsFilterMapper.mapAndGroupDonators(donatorsWithAmounts);
    log.debug("Donators after mapping: {}", finalDonators.size());

    final long sum = StreamEx.of(finalDonators)
      .mapToLong(Donator::amount)
      .sum();
    log.info("Total amount donated for period {}-{}: {}", startDate, endDate, sum);

    if (request.readOnly()) {
      log.info("Read-only mode. Skipping table update");
    } else {
      log.info("Updating donators table");
      final var namedDonators = donatorsFilterMapper.filterNamedDonators(finalDonators);
      donatorsTableUpdater.updateDonatorsTable(namedDonators);
    }
    return null;
  }

  private LocalDate createEndOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusDays(1);
  }

  private LocalDate createStartOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusMonths(properties.monthBefore());
  }
}
