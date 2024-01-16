package com.yuriytkach.batb.common.storage.ssm;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "app.storage.ssm")
public interface SsmProperties {

  @WithDefault("/batb/")
  String prefix();

  @WithDefault("/tokens")
  String tokens();

  @WithDefault("/accounts")
  String accounts();

  @WithDefault("statuses")
  String statuses();

}
