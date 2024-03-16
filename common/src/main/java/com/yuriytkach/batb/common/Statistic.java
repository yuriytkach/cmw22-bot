package com.yuriytkach.batb.common;

import java.time.Instant;
import java.util.Collection;

public record Statistic(Collection<StatisticData> stats, long totalAmount, Instant updatedAt) { }
