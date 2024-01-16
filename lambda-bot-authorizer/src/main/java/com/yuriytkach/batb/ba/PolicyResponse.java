package com.yuriytkach.batb.ba;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

/**
 * AWS Java SDK as well as documentation for Lambda Authorizer response is fucking broken!
 * Their {@link IamPolicyResponse} class defines that `Action` in policy document's `statement` is a String.
 * However, API Gateway accepts only array there.
 * Thus created this own class to build data that correctly serialized.
 * <ul>See also:
 * <li>CubePolicyResponseTest</li>
 * <li><a href="
 * https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-lambda-authorizer-output.html">
 * Output from an Amazon API Gateway Lambda authorizer</a></li>
 * </ul>
 */
@Data
@Builder(toBuilder = true)
@RegisterForReflection
public class PolicyResponse {

  @Nullable
  private final String principalId;

  @NonNull
  private final PolicyDocument policyDocument;

  @Builder.Default
  private final Map<String, Object> context = Map.of();

  @Data
  @Builder(toBuilder = true)
  @RegisterForReflection
  public static class PolicyDocument {
    @Builder.Default
    @JsonProperty("Version")
    private final String version = IamPolicyResponse.VERSION_2012_10_17;

    @Singular
    @JsonProperty("Statement")
    private final List<PolicyStatement> statements;
  }

  @Data
  @Builder(toBuilder = true)
  @RegisterForReflection
  public static class PolicyStatement {

    @NonNull
    @JsonProperty("Effect")
    private final String effect;

    @JsonProperty("Action")
    @Builder.Default
    private final List<String> actions = List.of(IamPolicyResponse.EXECUTE_API_INVOKE);

    @Singular
    @JsonProperty("Resource")
    private final List<String> resources;
  }
}
