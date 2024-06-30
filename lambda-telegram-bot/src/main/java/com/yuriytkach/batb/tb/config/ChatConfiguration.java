package com.yuriytkach.batb.tb.config;

import java.util.List;
import java.util.Map;

import com.yuriytkach.batb.common.telega.TelegramBotNotificationType;

public record ChatConfiguration(List<ChatConfig> chats) {

  public record ChatConfig(
    String id,
    Map<TelegramBotNotificationType, Integer> threads
  ) { }

}
