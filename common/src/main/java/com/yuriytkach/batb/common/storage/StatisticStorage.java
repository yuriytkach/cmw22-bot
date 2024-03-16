package com.yuriytkach.batb.common.storage;

import java.util.Optional;

import com.yuriytkach.batb.common.Statistic;

public interface StatisticStorage {

  Optional<Statistic> get();

  void save(Statistic statistic);

}
