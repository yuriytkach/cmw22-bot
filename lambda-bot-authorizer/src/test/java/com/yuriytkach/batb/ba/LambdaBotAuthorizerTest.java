package com.yuriytkach.batb.ba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.yuriytkach.batb.common.secret.SecretsReader;

@ExtendWith(MockitoExtension.class)
class LambdaBotAuthorizerTest {

  private static final String METHOD_ARN = "arn:aws:execute-api:eu-west-1:123456789012:abcdef12345/*/GET/";

  @Mock
  private SecretsReader secretsReader;

  @Mock
  private AppProperties appProperties;

  @Mock
  private ResponseCreator responseCreator;

  @Mock
  private PolicyResponse mockResponse;

  @Mock
  private Context contextMock;

  @InjectMocks
  private LambdaBotAuthorizer tested;

  @BeforeEach
  void initMocks() {
    when(contextMock.getAwsRequestId()).thenReturn("awsRequestId");
    when(appProperties.telegramHeader()).thenReturn("telegram");
    when(responseCreator.createResponse(anyString(), anyString())).thenReturn(mockResponse);
  }

  @Nested
  class AllowTests {

    @Test
    void shouldAllowRequestWhenTelegramHeaderIsPresentAndSecretIsCorrect() {
      final var input = AuthorizerRequest.builder()
        .methodArn(METHOD_ARN)
        .headers(Map.of("telegram", "token"))
        .build();

      when(appProperties.secretName()).thenReturn("secretName");
      when(secretsReader.readSecret("secretName")).thenReturn(Optional.of("token"));

      final var result = tested.handleRequest(input, contextMock);
      assertThat(result).isEqualTo(mockResponse);

      verify(responseCreator).createResponse(IamPolicyResponse.ALLOW, METHOD_ARN);
    }
  }

  @Nested
  class DenyTests {

    @Test
    void shouldDenyRequestWhenTelegramHeaderIsNotPresent() {
      final var input = AuthorizerRequest.builder()
        .methodArn(METHOD_ARN)
        .headers(Map.of())
        .build();

      final var result = tested.handleRequest(input, contextMock);
      assertThat(result).isEqualTo(mockResponse);

      verify(responseCreator).createResponse(IamPolicyResponse.DENY, METHOD_ARN);
      verifyNoInteractions(secretsReader);
    }

    @Test
    void shouldDenyRequestWhenTelegramHeaderIsPresentButSecretIsNotPresent() {
      final var input = AuthorizerRequest.builder()
        .methodArn(METHOD_ARN)
        .headers(Map.of("telegram", "token"))
        .build();

      when(appProperties.secretName()).thenReturn("secretName");
      when(secretsReader.readSecret("secretName")).thenReturn(Optional.empty());

      final var result = tested.handleRequest(input, contextMock);
      assertThat(result).isEqualTo(mockResponse);

      verify(responseCreator).createResponse(IamPolicyResponse.DENY, METHOD_ARN);
    }

    @Test
    void shouldDenyRequestWhenTelegramHeaderIsPresentButSecretIsNotCorrect() {
      final var input = AuthorizerRequest.builder()
        .methodArn(METHOD_ARN)
        .headers(Map.of("telegram", "token"))
        .build();

      when(appProperties.secretName()).thenReturn("secretName");
      when(secretsReader.readSecret("secretName")).thenReturn(Optional.of("incorrectToken"));

      final var result = tested.handleRequest(input, contextMock);
      assertThat(result).isEqualTo(mockResponse);

      verify(responseCreator).createResponse(IamPolicyResponse.DENY, METHOD_ARN);
    }
  }

}
