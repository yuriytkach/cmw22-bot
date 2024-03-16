package com.yuriytkach.batb.status.api;

import java.util.Optional;

import com.yuriytkach.batb.common.Statistic;
import com.yuriytkach.batb.common.storage.StatisticStorage;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class StatsService {

  static final String CACHE_STATS = "stats";

  private final StatisticStorage statisticStorage;


  @CacheResult(cacheName = CACHE_STATS)
  public Optional<Statistic> getStatistic() {
    log.info("Retrieve stats...");
    return statisticStorage.get();
  }
}
