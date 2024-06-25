package com.yuriytkach.batb.bc.notify;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ai.lambda")
public interface AiLambdaConfig {
  String functionName();
}
