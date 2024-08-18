/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
