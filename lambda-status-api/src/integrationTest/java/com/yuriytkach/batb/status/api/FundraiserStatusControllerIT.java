package com.yuriytkach.batb.status.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.storage.FundInfoStorage;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@TestHTTPEndpoint(FundraiserStatusController.class)
class FundraiserStatusControllerIT {

  @InjectMock
  FundInfoStorage fundInfoStorage;

  @Inject
  @CacheName(FundService.CACHE_FUND_STATUS)
  Cache cache;

  @BeforeEach
  void invalidateCache() {
    cache.invalidateAll().await().indefinitely();
  }

  @Test
  void shouldReturnNotFoundIfFundDoesNotExist() {
    when(fundInfoStorage.getById(anyString())).thenReturn(Optional.empty());

    given()
      .when().get("/fundId")
      .then()
      .statusCode(404);

    verify(fundInfoStorage).getById("fundId");
  }

  @Test
  void shouldReturnFundStatusIfFundExists() {
    when(fundInfoStorage.getById(anyString())).thenReturn(Optional.of(defaultFundInfo()));

    final var actual = given()
      .when()
      .get("/fundId")
      .then()
      .statusCode(200)
      .extract()
      .body().as(FundraiserStatusResponse.class);

    assertThat(actual).isEqualTo(expectedFundResponse());

    verify(fundInfoStorage).getById("fundId");
  }

  @Test
  void shouldReturnFundStatusFromCacheIfFundExists() {
    when(fundInfoStorage.getById(anyString())).thenReturn(Optional.of(defaultFundInfo()));

    given()
      .when()
      .get("/fundId")
      .then()
      .statusCode(200);

    given()
      .when()
      .get("/fundId")
      .then()
      .statusCode(200);

    verify(fundInfoStorage, atMostOnce()).getById("fundId");
  }

  @Test
  void shouldReturnCorsAndCacheHeaders() {
    when(fundInfoStorage.getById(anyString())).thenReturn(Optional.of(defaultFundInfo()));

    final String origin = "http://custom.origin.quarkus";
    final String methods = "GET";

    given()
      .header("Origin", origin)
      .header("Access-Control-Request-Method", methods)
      .when()
      .get("/fundId")
      .then()
      .statusCode(200)
      .header("Access-Control-Allow-Origin", origin)
      .header("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS")
      .header("Access-Control-Allow-Credentials", "false")
      .header("Cache-Control", "no-transform, max-age=60");

    verify(fundInfoStorage).getById("fundId");
  }

  private FundraiserStatusResponse expectedFundResponse() {
    return FundraiserStatusResponse.builder()
      .goal(1000)
      .name("fundName")
      .description("fundDescription")
      .raised(50)
      .spent(6)
      .lastUpdatedAt(Instant.parse("2021-01-01T00:00:00Z"))
      .active(true)
      .build();
  }

  private FundInfo defaultFundInfo() {
    return FundInfo.builder()
      .goal(1000)
      .name("fundName")
      .description("fundDescription")
      .lastRaised(5000)
      .lastSpent(600)
      .lastUpdatedAt(Instant.parse("2021-01-01T00:00:00Z"))
      .id("fundId")
      .active(true)
      .build();
  }
}
