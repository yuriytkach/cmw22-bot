package com.yuriytkach.batb.dr;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "reporter")
public interface DonatorsReporterProperties {

  @WithDefault("0")
  int monthBefore();

}
