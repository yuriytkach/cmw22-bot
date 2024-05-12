package com.yuriytkach.batb.dr.tx.mono.token.api;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Client(String clientId, String name) { }
