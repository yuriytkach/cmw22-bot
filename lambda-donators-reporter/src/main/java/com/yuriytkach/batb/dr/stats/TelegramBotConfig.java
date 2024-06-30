package com.yuriytkach.batb.dr.stats;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "bot.telegram")
public interface TelegramBotConfig {
  String secretName();
}
