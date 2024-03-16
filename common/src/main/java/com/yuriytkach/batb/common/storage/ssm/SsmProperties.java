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

  @WithDefault("/funds/")
  String funds();

  @WithDefault("/stats")
  String statistics();

  @WithDefault("current")
  String currentFundProp();

  @WithDefault("gsheet/registry")
  String registryGsheet();

  @WithDefault("gsheet/stats")
  String statsGsheet();
}
