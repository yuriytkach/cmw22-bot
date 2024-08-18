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

package com.yuriytkach.batb.tb.config;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuriytkach.batb.common.secret.SecretsReader;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ChatConfigReader {

  private final SecretsReader secretsReader;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;

  public Optional<ChatConfiguration> readConfig() {
    return secretsReader.readSecret(appProperties.chatIdSecretKey())
      .map(secret -> {
        try {
          return objectMapper.readValue(secret, ChatConfiguration.class);
        } catch (final Exception ex) {
          log.error("Error while reading chat config: {}", ex.getMessage());
          return null;
        }
      });
  }
}
