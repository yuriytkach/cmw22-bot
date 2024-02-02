package com.yuriytkach.status.api;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record FundraiserStatus(
  int goal,
  int raised,
  int spent,
  String name,
  String description,
  Instant lastUpdatedAt
) {

}
