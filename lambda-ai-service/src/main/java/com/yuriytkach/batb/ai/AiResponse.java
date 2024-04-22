package com.yuriytkach.batb.ai;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record AiResponse(Evaluation evaluation, String message) {

  @JsonCreator
  public AiResponse {
  }

}
