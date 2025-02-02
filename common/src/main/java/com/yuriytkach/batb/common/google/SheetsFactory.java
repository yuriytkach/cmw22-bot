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

package com.yuriytkach.batb.common.google;

import static java.util.function.Predicate.not;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.yuriytkach.batb.common.BankToken;
import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.storage.BankAccessStorage;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class SheetsFactory {

  static final List<String> GSHEET_ACCESS_SCOPE = List.of(SheetsScopes.SPREADSHEETS);
  public static final String APPLICATION_NAME = "CMW22 Bot";
  public static final String EMAIL_TOKEN_NAME = "email";

  private final BankAccessStorage bankAccessStorage;

  public Optional<Sheets> getSheetsService() {
    return loadAccessData().flatMap(this::createSheetsService);
  }

  private Optional<GSheetAccessData> loadAccessData() {
    final List<BankToken> tokens = bankAccessStorage.getListOfTokens(BankType.GSHEET);
    log.debug("Loaded gsheet tokens: {}", tokens.size());

    if (tokens.isEmpty()) {
      log.warn("No tokens found for GSheet");
      return Optional.empty();
    } else {
      final Predicate<BankToken> emailPredicate = token -> token.name().endsWith(EMAIL_TOKEN_NAME);
      final var emailOpt = tokens.stream().filter(emailPredicate).findFirst();
      final var tokenOpt = tokens.stream().filter(not(emailPredicate)).findFirst();

      if (emailOpt.isPresent() && tokenOpt.isPresent()) {
        return Optional.of(new GSheetAccessData(emailOpt.get().value(), tokenOpt.get().value()));
      } else {
        final var allTokenNames = tokens.stream().map(BankToken::name).collect(Collectors.joining(","));
        log.warn(
          "Either email ({}) or token ({}) not found for GSheet: {}",
          emailOpt.isPresent(),
          tokenOpt.isPresent(),
          allTokenNames
        );
        return Optional.empty();
      }
    }
  }

  private Optional<Sheets> createSheetsService(final GSheetAccessData gSheetAccessData) {
    try {
      final GoogleCredentials credentials = GoogleCredentials.fromStream(
          new ByteArrayInputStream(gSheetAccessData.token().getBytes())
        )
        .createDelegated(gSheetAccessData.email())
        .createScoped(GSHEET_ACCESS_SCOPE);

      final Sheets sheets = new Sheets.Builder(
        new NetHttpTransport(),
        new GsonFactory(),
        new HttpCredentialsAdapter(credentials)
      )
        .setApplicationName(APPLICATION_NAME)
        .build();

      return Optional.of(sheets);
    } catch (final Exception ex) {
      log.error("Failed to create Google Sheets service: {}", ex.getMessage(), ex);
      return Optional.empty();
    }
  }

  record GSheetAccessData(String email, String token) { }
}
