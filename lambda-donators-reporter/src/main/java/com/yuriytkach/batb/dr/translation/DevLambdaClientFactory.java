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

package com.yuriytkach.batb.dr.translation;

import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@ApplicationScoped
@IfBuildProfile("dev")
public class DevLambdaClientFactory {

  @Inject
  DevLambdaClientProperties properties;

  @Produces
  @ApplicationScoped
  LambdaClient createLambdaClient() {
    return LambdaClient.builder()
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
      ))
      .region(Region.of(properties.region()))
      .build();
  }

  @IfBuildProfile("dev")
  @ConfigMapping(prefix = "dev-lambda-client")
  public interface DevLambdaClientProperties {
    @WithDefault("accessKey")
    String accessKey();
    @WithDefault("secretKey")
    String secretKey();
    @WithDefault("eu-west-1")
    String region();
  }

}
