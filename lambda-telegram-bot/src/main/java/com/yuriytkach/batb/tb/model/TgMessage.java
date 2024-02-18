package com.yuriytkach.batb.tb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record TgMessage(
  @JsonProperty("chat_id")
  Long chatId,

  @JsonProperty("parse_mode")
  String parseMode,

  String text
) {

}
