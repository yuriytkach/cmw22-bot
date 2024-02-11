package com.yuriytkach.status.api;

import java.util.Optional;

import com.yuriytkach.batb.common.FundStatus;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class FundService {

  private final FundInfoStorage fundInfoStorage;

  @CacheResult(cacheName = "fund-status")
  public Optional<FundStatus> getFundStatus(final String fundraiserId) {
    log.info("Get Fundraiser Info: {}", fundraiserId);
    return fundInfoStorage.getById(fundraiserId)
      .map(fundInfo -> FundStatus.builder()
        .info(fundInfo)
        .raised(fundInfo.lastRaised())
        .spent(fundInfo.lastSpent())
        .updatedAt(fundInfo.lastUpdatedAt())
        .hasUpdates(false)
        .build());
  }
}
