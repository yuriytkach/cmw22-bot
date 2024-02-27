package com.yuriytkach.batb.ba;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResponseCreatorTest {

  @Test
  void shouldCreateResponseFromParams() {
    final ResponseCreator tested = new ResponseCreator();

    final PolicyResponse actual = tested.createResponse("effect", "arn");

    assertThat(actual).isEqualTo(PolicyResponse.builder()
      .policyDocument(PolicyResponse.PolicyDocument.builder()
        .statement(PolicyResponse.PolicyStatement.builder()
          .effect("effect")
          .resource("arn")
          .build())
        .build())
      .principalId("principalId")
      .build());
  }

}
