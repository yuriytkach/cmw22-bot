package com.yuriytkach.batb.common;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record StatusUpdateRequest(UpdateType check) {

  @RegisterForReflection
  public enum UpdateType {
    CURRENT,
    FULL
  }

}
