package com.yuriytkach.batb.ba;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppProperties {

  String secretName();

  String telegramHeader();
}
