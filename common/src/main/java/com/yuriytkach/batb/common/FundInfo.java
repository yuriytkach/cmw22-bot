package com.yuriytkach.batb.common;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record FundInfo(
  String id,
  List<String> accounts,
  String name,
  String description,
  long goal,
  long lastRaised,
  long lastSpent,
  Instant lastUpdatedAt
) { }
