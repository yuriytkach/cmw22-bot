package com.yuriytkach.batb.dr;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    final var finalDonators = donatorsFilterMapper.mapAndGroupDonators(donatorsWithAmounts);
    log.debug("Donators after mapping and grouping: {}", finalDonators.grouped().size());

    outputStats(finalDonators, startDate, endDate);

    if (request.readOnly()) {
      log.info("Read-only mode. Skipping table update");
    } else {
      log.info("Updating donators table");
      final var namedDonators = donatorsFilterMapper.filterNamedDonators(finalDonators.grouped());
      donatorsTableUpdater.updateDonatorsTable(namedDonators);
    }
    return null;
  }

  private void outputStats(
    final DonatorsFilterMapper.DonatorSets finalDonators,
    final LocalDate startDate,
    final LocalDate endDate
  ) {
    final long sum = StreamEx.of(finalDonators.grouped())
      .mapToLong(Donator::amount)
      .sum();
    log.info("Total amount donated for period {}-{}: {}", startDate, endDate, sum);

    List<Long> amounts = finalDonators.ungrouped().stream()
      .map(Donator::amount)
      .sorted()
      .toList();

    statsForListOfLongs(startDate, endDate, amounts, "amount");

    List<Long> counts = finalDonators.grouped().stream()
      .map(Donator::count)
      .map(Integer::longValue)
      .sorted()
      .toList();

    statsForListOfLongs(startDate, endDate, counts, "count");
  }

  private void statsForListOfLongs(
    final LocalDate startDate,
    final LocalDate endDate,
    final List<Long> amounts,
    final String statName
  ) {
    Optional<Long> maxAmount = amounts.stream().max(Long::compare);
    Optional<Long> minAmount = amounts.stream().min(Long::compare);
    double averageAmount = amounts.stream().mapToLong(Long::longValue).average().orElse(0.0);
    double medianAmount = getPercentile(amounts, 50);
    double p10Amount = getPercentile(amounts, 10);
    double p90Amount = getPercentile(amounts, 90);
    double p95Amount = getPercentile(amounts, 95);

    log.info(
      "Donation {} stats for period {}-{}: max={}, min={}, avg={}, median={}, p90={}, p95={}, p10={}",
      statName,
      startDate,
      endDate,
      maxAmount.orElse(0L), minAmount.orElse(0L), averageAmount, medianAmount, p90Amount,
      p95Amount,
      p10Amount
    );
  }

  private double getPercentile(List<Long> sortedList, double percentile) {
    if (sortedList.isEmpty()) {
      return 0;
    }
    int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
    return sortedList.get(index);
  }

  private LocalDate createEndOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusMonths(properties.monthBefore() - 1).minusDays(1);
  }

  private LocalDate createStartOfMonthDate() {
    return LocalDate.now(clock).withDayOfMonth(1).minusMonths(properties.monthBefore());
  }
}
