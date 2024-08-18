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

package com.yuriytkach.batb.dr.tx.privat;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.yuriytkach.batb.common.BankToken;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.storage.BankAccessStorage;
import com.yuriytkach.batb.dr.tx.DonationTransaction;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;

@QuarkusTest
@QuarkusTestResource(WireMockResource.class)
class PrivatTxFetcherIT {

  @Inject
  PrivatTxFetcher privatTxFetcher;

  @InjectMock
  SecretsReader secretsReader;
  @InjectMock
  BankAccessStorage bankAccessStorage;

  @BeforeAll
  static void initWireMock() {
    WireMock.configureFor("http", WireMockResource.getContainerHost(), WireMockResource.getMappedPort());
  }

  @BeforeEach
  void resetWiremock() {
    WireMock.reset();
  }

  @Test
  @SneakyThrows
  void shouldLoadTransactions() {
    when(secretsReader.readSecret(anyString())).thenReturn(Optional.of(
      """
      {
        "tokenName": "ttt",
        "accounts": ["account1"]
      }
      """
    ));
    when(bankAccessStorage.getListOfTokens(any())).thenReturn(List.of(
      new BankToken("aaa", "bbb"),
      new BankToken("ttt", "validToken")
    ));

    stubFor(
      get(urlPathEqualTo("/api/statements/transactions"))
        .willReturn(aResponse()
          .withHeader("Content-Type", "application/json")
          .withStatus(200)
          .withBody(Files.readString(Path.of(this.getClass().getResource("/sample-privatbank-txes.json").toURI())))
        )
    );

    final Set<DonationTransaction> txes = privatTxFetcher.fetchTransactions(
      LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28));

    assertThat(txes).containsExactlyInAnyOrder(
      DonationTransaction.builder()
        .id("D2P74444444VYY")
        .name("Бандера Степан")
        .amountUah(44445)
        .date(Instant.parse("2024-02-02T15:23:00Z"))
        .build(),
      DonationTransaction.builder()
        .id("COII666666VZ7K")
        .name("Bandera Stepan")
        .amountUah(50000)
        .date(Instant.parse("2024-02-07T20:39:00Z"))
        .build(),
      DonationTransaction.builder()
        .id("JBKLO55555550ZI")
        .name("ЙОЙО ТОВ")
        .amountUah(55500)
        .date(Instant.parse("2024-04-08T11:39:00Z"))
        .build(),
      DonationTransaction.builder()
        .id("HS438808L0KKR")
        .name("Бандера Степан")
        .amountUah(8800)
        .date(Instant.parse("2024-02-08T18:04:00Z"))
        .build(),
      DonationTransaction.builder()
        .id("D2BP777777741PP")
        .name("Шухевич Роман")
        .amountUah(50000)
        .date(Instant.parse("2024-02-07T20:47:00Z"))
        .build(),
      DonationTransaction.builder()
        .id("COII555555H6C6")
        .name("Bandera Stepan")
        .amountUah(10000)
        .date(Instant.parse("2024-02-04T05:46:00Z"))
        .build()
    );
  }
}
