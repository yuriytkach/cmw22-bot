package com.yuriytkach.batb.bc.notify;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "bot.telegram")
public interface TelegramBotConfig {
  String secretName();
}
