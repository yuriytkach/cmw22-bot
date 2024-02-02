package com.yuriytkach.batb.tb.config;

import org.telegram.telegrambots.meta.api.interfaces.BotApiObject;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(
  targets = {
    BotApiObject.class
  },
  registerFullHierarchy = true
)
public class ReflectionConfiguration {

}
