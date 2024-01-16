package com.yuriytkach.batb.ba;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
class ResponseCreator {

  PolicyResponse createResponse(
    final String effect,
    final String resourceArn
  ) {
    log.info("{}} - {}", effect, resourceArn);

    final PolicyResponse.PolicyStatement statement = PolicyResponse.PolicyStatement.builder()
      .effect(effect)
      .resource(resourceArn)
      .build();

    final PolicyResponse.PolicyDocument policyDocument = PolicyResponse.PolicyDocument.builder()
      .statement(statement)
      .build();

    return PolicyResponse.builder().policyDocument(policyDocument)
      .principalId("principalId")
      .build();
  }
}
