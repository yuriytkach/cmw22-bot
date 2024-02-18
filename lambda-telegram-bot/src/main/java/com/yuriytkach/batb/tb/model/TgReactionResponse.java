package com.yuriytkach.batb.tb.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
@RegisterForReflection
public class TgReactionResponse {

  @Builder.Default
  private final String method = "setMessageReaction";

  @JsonProperty("chat_id")
  private final Long chatId;

  @JsonProperty("message_id")
  private final Integer messageId;

  @JsonProperty("is_big")
  boolean isBig;

  @Singular("react")
  private final List<TgReaction> reaction;
}
