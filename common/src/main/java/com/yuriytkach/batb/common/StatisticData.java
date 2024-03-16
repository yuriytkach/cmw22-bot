package com.yuriytkach.batb.common;

import lombok.Builder;

@Builder
public record StatisticData(
  String id,
  long count,
  long amount,
  long amountPercent
) { }
