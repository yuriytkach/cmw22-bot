package com.yuriytkach.batb.dr.config;

import java.time.Clock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class CommonBeans {

  @Produces
  @ApplicationScoped
  Clock clock() {
    return Clock.systemUTC();
  }

}
