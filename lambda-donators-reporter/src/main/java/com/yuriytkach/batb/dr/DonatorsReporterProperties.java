package com.yuriytkach.batb.dr;

import java.util.Map;
import java.util.Set;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "reporter")
public interface DonatorsReporterProperties {

  @WithDefault("0")
  int monthBefore();

  @WithDefault("50000")
  long minAmountCents();

  @WithDefault("3")
  int minCount();

  @WithDefault("/batb/tx/ignored-names")
  String ignoredDonatorsKey();

  Set<String> nameStopWords();

  Map<String, String> replacers();

}
