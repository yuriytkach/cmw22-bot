package com.yuriytkach.batb.tb.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppProperties {

  String chatIdSecretKey();

  String botName();
}
