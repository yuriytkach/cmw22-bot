package com.yuriytkach.batb.dr;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record DonatorsReporterLambdaRequest(boolean readOnly) { }
