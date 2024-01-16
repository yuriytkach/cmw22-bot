package com.yuriytkach.batb.tb.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "commands")
public interface CommandsProperties {

  String status();
}
