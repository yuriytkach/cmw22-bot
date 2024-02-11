package com.yuriytkach.batb.bsu;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.FundStatus;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
public class FundService {

  @Inject
  FundInfoStorage fundInfoStorage;

  public void updateCurrentFundraiser(final FundInfo fundInfo, final List<BankAccountStatus> statuses) {
    log.info("Update current fundraiser `{}` with statuses: {}", fundInfo.id(), statuses.size());
    log.debug("Bank statuses: {}", statuses);

    final FundStatus fundStatus = calculateFundAmounts(fundInfo, statuses);

    if (fundStatus.hasUpdates()) {
      updateFundInfoInStorage(fundStatus);
    } else {
      log.info("No updates for fundraiser: {}", fundInfo.id());
    }
  }

  private FundStatus calculateFundAmounts(final FundInfo fundInfo, final List<BankAccountStatus> statuses) {
    log.info("Calculate fund amounts for: {}", fundInfo);

    if (statuses.isEmpty()) {
      log.info("No account statuses received for fundraiser: {} Returning last known data", fundInfo.name());
      return FundStatus.builder()
        .info(fundInfo)
        .raised(fundInfo.lastRaised())
        .spent(fundInfo.lastSpent())
        .updatedAt(fundInfo.lastUpdatedAt())
        .hasUpdates(false)
        .build();
    }

    final Instant maxUpdatedAt = StreamEx.of(statuses)
      .map(BankAccountStatus::updatedAt)
      .max(Comparator.naturalOrder())
      .orElseThrow();
    log.info("Max updated at: {}", maxUpdatedAt);

    final long raised = StreamEx.of(statuses)
      .mapToLong(BankAccountStatus::amountUah)
      .sum();

    final long spent = StreamEx.of(statuses)
      .mapToLong(status -> status.spentAmountUah() == null ? 0L : status.spentAmountUah())
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
