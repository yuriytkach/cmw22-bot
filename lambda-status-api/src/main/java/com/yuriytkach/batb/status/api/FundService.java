package com.yuriytkach.batb.status.api;

import java.util.Optional;

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

  private final FundInfoStorage fundInfoStorage;

  @CacheResult(cacheName = CACHE_FUND_STATUS)
  public Optional<FundraiserStatusResponse> getFundStatus(final String fundraiserId) {
    log.info("Retrieve fund Info: {}", fundraiserId);
    return fundInfoStorage.getById(fundraiserId)
      .map(fundInfo -> FundraiserStatusResponse.builder()
        .goal(fundInfo.goal())
        .raised(fundInfo.lastRaised() / 100)
        .spent(fundInfo.lastSpent() / 100)
        .name(fundInfo.name())
        .description(fundInfo.description())
        .lastUpdatedAt(fundInfo.lastUpdatedAt())
        .build());
  }
}
