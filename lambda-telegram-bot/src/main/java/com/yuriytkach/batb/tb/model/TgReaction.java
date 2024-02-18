package com.yuriytkach.batb.tb.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TgReaction(
  String type,
  String emoji
) {

  public static TgReaction thumbsUp() {
    return new TgReaction("emoji", "👍");
  }

  public static TgReaction thinking() {
    return new TgReaction("emoji", "🤔");
  }
}
