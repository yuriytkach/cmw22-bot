package com.yuriytkach.status.api;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.AccountBalanceStorage;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FundService {

  private final AccountBalanceStorage accountBalanceStorage;
  private final FundInfoStorage fundInfoStorage;

  @CacheResult(cacheName = "fund-status")
  public Optional<FundStatus> getFundStatus(final String fundraiserId) {
    log.info("Get Fundraiser Info: {}", fundraiserId);
    final Optional<FundStatus> fundStatus = fundInfoStorage.getById(fundraiserId)
      .map(this::calculateFundAmounts);

    fundStatus.filter(FundStatus::hasUpdates)
      .ifPresentOrElse(
        this::updateFundInfoInStorage,
        () -> log.info("No updates for fundraiser: {}", fundraiserId)
      );

    return fundStatus;
  }

  private FundStatus calculateFundAmounts(final FundInfo fundInfo) {
    log.info("Calculate fund amounts for: {}", fundInfo);

    final var fundAccountStatuses = StreamEx.of(fundInfo.accounts())
      .map(accountBalanceStorage::getById)
      .flatMap(Optional::stream)
      .toImmutableList();

    if (fundAccountStatuses.isEmpty()) {
      log.info("No account statuses found for fundraiser: {} Returning last known data", fundInfo.name());
      return FundStatus.builder()
        .info(fundInfo)
        .raised(fundInfo.lastRaised())
        .spent(fundInfo.lastSpent())
        .updatedAt(fundInfo.lastUpdatedAt())
        .hasUpdates(false)
        .build();
    }

    final Instant maxUpdatedAt = StreamEx.of(fundAccountStatuses)
      .map(BankAccountStatus::updatedAt)
      .max(Comparator.naturalOrder())
      .orElseThrow();
    log.info("Max updated at: {}", maxUpdatedAt);

    final long raised = StreamEx.of(fundAccountStatuses)
      .filter(status -> status.amount() > 0)
      .mapToLong(BankAccountStatus::amountUah)
      .sum();

    final long spent = StreamEx.of(fundAccountStatuses)
      .filter(status -> status.amount() < 0)
      .mapToLong(BankAccountStatus::amountUah)
      .sum();

    final long totalRaised = raised + Math.abs(spent);

    log.info("Raised: {} Spent: {} Total Raised: {}", raised, spent, totalRaised);

    return FundStatus.builder()
      .info(fundInfo)
      .raised(Math.max(totalRaised, fundInfo.lastRaised()))
      .spent(Math.max(Math.abs(spent), fundInfo.lastSpent()))
      .updatedAt(maxUpdatedAt)
      .hasUpdates(maxUpdatedAt.isAfter(fundInfo.lastUpdatedAt()))
      .build();
  }

  private void updateFundInfoInStorage(FundStatus fundStatus) {
    final var fundInfo = FundInfo.builder()
      .id(fundStatus.info().id())
      .accounts(fundStatus.info().accounts())
      .name(fundStatus.info().name())
      .description(fundStatus.info().description())
      .goal(fundStatus.info().goal())
      .lastRaised(fundStatus.raised())
      .lastSpent(fundStatus.spent())
      .lastUpdatedAt(fundStatus.updatedAt())
      .build();

    fundInfoStorage.save(fundInfo);
  }
}
