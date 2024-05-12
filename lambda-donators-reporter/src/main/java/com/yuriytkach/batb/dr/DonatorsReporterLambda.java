package com.yuriytkach.batb.dr;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.dr.gsheet.DonatorsTableUpdater;
import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.DonationTransactionFetcher;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@Named("donatorsReporter")
public class DonatorsReporterLambda implements RequestHandler<Object, Void> {

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
  public Void handleRequest(final Object ignored, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Donators Reporter Lambda. AWS Request ID: {}", context.getAwsRequestId());

    final LocalDate startDate = createStartOfMonthDate();
    final LocalDate endDate = createEndOfMonthDate();

    log.debug("Fetching transactions in date range: {} - {}", startDate, endDate);

    final Map<String, Long> donatorsWithAmounts = StreamEx.of(txFetchers.stream())
      .map(fetcher -> fetcher.fetchTransactions(startDate, endDate))
      .flatMap(Collection::stream)
      .mapToEntry(DonationTransaction::name, DonationTransaction::amountUah)
      .grouping(Collectors.summingLong(Long::valueOf));

    final Map<String, Long> finalDonators = donatorsFilterMapper.filterAndMapDonators(donatorsWithAmounts);

    log.info("Donators with amounts after filtering: {}", finalDonators.size());
    log.debug("Donators with amounts after filtering: {}", finalDonators);

    donatorsTableUpdater.updateDonatorsTable(finalDonators);
    return null;
  }

  private LocalDate createEndOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusDays(1);
  }

  private LocalDate createStartOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusMonths(properties.monthBefore());
  }
}
