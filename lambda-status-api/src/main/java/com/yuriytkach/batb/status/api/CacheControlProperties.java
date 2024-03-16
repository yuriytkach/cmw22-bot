package com.yuriytkach.batb.status.api;

import java.time.Duration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.cache-control")
public interface CacheControlProperties {

  MaxAge maxAge();

  interface MaxAge {
    Duration fundStatus();
    Duration stats();
  }

}
