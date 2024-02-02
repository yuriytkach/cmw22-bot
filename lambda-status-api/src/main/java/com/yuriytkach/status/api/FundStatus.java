package com.yuriytkach.status.api;

import java.time.Instant;

import com.yuriytkach.batb.common.FundInfo;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record FundStatus(
  FundInfo info,
  long raised,
  long spent,
  Instant updatedAt,
  boolean hasUpdates
) { }
