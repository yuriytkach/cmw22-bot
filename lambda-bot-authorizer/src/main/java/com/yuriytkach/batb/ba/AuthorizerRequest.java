package com.yuriytkach.batb.ba;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;

@Builder
@SuppressFBWarnings
@RegisterForReflection
public record AuthorizerRequest(
  String type,
  String methodArn,
  String resource,
  String path,
  String httpMethod,
  Map<String, String> headers,
  Map<String, List<String>> multiValueHeaders,
  Map<String, String> queryStringParameters,
  Map<String, List<String>> multiValueQueryStringParameters,
  Map<String, String> pathParameters,
  Map<String, String> stageVariables,
  APIGatewayCustomAuthorizerEvent.RequestContext requestContext
) { }
