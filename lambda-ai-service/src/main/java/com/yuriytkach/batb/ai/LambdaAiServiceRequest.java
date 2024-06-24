package com.yuriytkach.batb.ai;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record LambdaAiServiceRequest(AiServiceType service, String payload) {

}
