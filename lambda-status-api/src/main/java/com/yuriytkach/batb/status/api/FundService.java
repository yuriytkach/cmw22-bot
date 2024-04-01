package com.yuriytkach.batb.status.api;

import java.util.Optional;

import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FundService {

  static final String CACHE_FUND_STATUS = "fund-status";

  static final String CURRENT_FUND_ID = "current";

  private final FundInfoStorage fundInfoStorage;

  @CacheResult(cacheName = CACHE_FUND_STATUS)
  public Optional<FundraiserStatusResponse> getFundStatus(final String fundraiserId) {
    final Optional<FundInfo> fundInfoOpt;

    if (CURRENT_FUND_ID.equalsIgnoreCase(fundraiserId)) {
      log.info("Retrieve current fund Info");
      fundInfoOpt = fundInfoStorage.getCurrent();
    } else {
      log.info("Retrieve fund Info: {}", fundraiserId);
      fundInfoOpt = fundInfoStorage.getById(fundraiserId);
    }

    return fundInfoOpt
      .map(fundInfo -> FundraiserStatusResponse.builder()
        .goal(fundInfo.goal())
        .raised(fundInfo.lastRaised() / 100)
        .spent(fundInfo.lastSpent() / 100)
        .name(fundInfo.name())
        .description(fundInfo.description())
        .lastUpdatedAt(fundInfo.lastUpdatedAt())
        .active(fundInfo.active())
        .build());
  }
}
