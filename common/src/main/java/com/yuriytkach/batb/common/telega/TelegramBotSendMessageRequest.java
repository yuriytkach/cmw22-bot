package com.yuriytkach.batb.common.telega;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TelegramBotSendMessageRequest(
  TelegramBotNotificationType notificationType,
  String message,
  String imgUrl
) {
}
