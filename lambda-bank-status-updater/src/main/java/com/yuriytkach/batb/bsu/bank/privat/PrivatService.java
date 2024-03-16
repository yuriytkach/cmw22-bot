package com.yuriytkach.batb.bsu.bank.privat;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.reducing;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.bsu.bank.privat.api.Balance;
import com.yuriytkach.batb.bsu.bank.privat.api.PrivatApi;
import com.yuriytkach.batb.common.BankToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
class PrivatService {

  public static final DateTimeFormatter START_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  public static final DateTimeFormatter DPD_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  @RestClient
  private final PrivatApi api;
  private final Clock clock;

  List<Balance> readAllAvailablePrivatAccounts(final BankToken token) {
    log.info("Reading privat accounts for token: {}", token.name());
    try {
      final String startDate = START_DATE_FORMATTER
        .withZone(clock.getZone()).format(clock.instant());

      final var balances = StreamEx.of(readAllBalances(token.value(), startDate, null))
        .groupingBy(Balance::acc, collectingAndThen(reducing(this::selectLatestByDpd), Optional::get))
        .values();

      log.info("Total loaded balances from privatbank: {}", balances.size());
      return List.copyOf(balances);

    } catch (final WebApplicationException ex) {
      final Response response = ex.getResponse();
      final String body;
      final int status;
      if (response == null) {
        body = ex.getMessage();
        status = -1;
      } else {
        body = response.hasEntity() ? response.readEntity(String.class) : "<no body>";
        status = response.getStatus();
      }
      log.warn("Failed to get all balances: [{}] {}", status, body);

    } catch (final ProcessingException ex) {
      log.error("Failed to get all balances: {}", ex.getMessage());
    }

    return List.of();
  }

  private Balance selectLatestByDpd(final Balance balance1, final Balance balance2) {
    final var dpd1 = DPD_FORMATTER.withZone(clock.getZone()).parse(balance1.dpd());
    final var dpd2 = DPD_FORMATTER.withZone(clock.getZone()).parse(balance2.dpd());
    return Instant.from(dpd1).compareTo(Instant.from(dpd2)) > 0 ? balance1 : balance2;
  }

  private Stream<Balance> readAllBalances(final String token, final String startDate, final String followId) {
    final var response = api.finalBalances(token, startDate, followId);
    final Stream<Balance> nextResponse;
    if (response.existsNextPage()) {
      nextResponse = readAllBalances(token, startDate, response.nextPageId());
    } else {
      nextResponse = Stream.empty();
    }
    return Stream.concat(response.balances().stream(), nextResponse);
  }

}
