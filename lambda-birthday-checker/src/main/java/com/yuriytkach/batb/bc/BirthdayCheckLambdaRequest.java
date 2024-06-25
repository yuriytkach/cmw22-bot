package com.yuriytkach.batb.bc;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record BirthdayCheckLambdaRequest(boolean readOnly, String overrideName) { }
