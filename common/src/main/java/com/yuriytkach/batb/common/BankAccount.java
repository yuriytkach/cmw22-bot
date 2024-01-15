package com.yuriytkach.batb.common;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record BankAccount(String id, String name) { }
