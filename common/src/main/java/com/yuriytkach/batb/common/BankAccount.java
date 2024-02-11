package com.yuriytkach.batb.common;

import java.util.Map;

import javax.annotation.Nullable;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@RegisterForReflection
public record BankAccount(String id, String name, @Nullable Map<String, String> properties) { }
