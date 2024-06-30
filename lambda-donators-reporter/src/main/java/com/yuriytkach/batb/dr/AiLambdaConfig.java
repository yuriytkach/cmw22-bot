package com.yuriytkach.batb.dr;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ai.lambda")
public interface AiLambdaConfig {
  String functionName();
}
