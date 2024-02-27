package com.yuriytkach.batb.status.api;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record FundraiserStatusResponse(
  long goal,
  long raised,
  long spent,
  String name,
  String description,
  Instant lastUpdatedAt
) {

}
