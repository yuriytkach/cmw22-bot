package com.yuriytkach.batb.dr.tx.privat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.yuriytkach.batb.common.BankType;
import com.yuriytkach.batb.common.json.JsonReader;
import com.yuriytkach.batb.common.secret.SecretsReader;
import com.yuriytkach.batb.common.storage.BankAccessStorage;
import com.yuriytkach.batb.dr.tx.DonationTransaction;
import com.yuriytkach.batb.dr.tx.DonationTransactionFetcher;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class PrivatTxFetcher implements DonationTransactionFetcher {

  private final SecretsReader secretsReader;
  private final BankAccessStorage bankAccessStorage;
  private final PrivatTxFetcherProperties props;
  private final JsonReader jsonReader;
  private final PrivatService privatService;

  @Override
  public Set<DonationTransaction> fetchTransactions(final LocalDate startDate) {
    return secretsReader.readSecret(props.configKey())
      .flatMap(value -> jsonReader.readValue(value, PrivatTxFetcherConfig.class))
      .map(config -> readTxes(config, startDate))
      .orElseGet(() -> {
        log.error("Cannot read privat TXes");
        return Set.of();
      });
  }

  private Set<DonationTransaction> readTxes(final PrivatTxFetcherConfig config, final LocalDate startDate) {
    log.info("Reading privat TXes for accounts: {} with token: {}", config.accounts(), config.tokenName());
    final var privatToken = bankAccessStorage.getListOfTokens(BankType.PRIVAT).stream()
      .filter(token -> token.name().equals(config.tokenName()))
      .findFirst();

    if (privatToken.isEmpty()) {
      log.error("Cannot find privat token with name: {}", config.tokenName());
      return Set.of();
    } else {
      log.debug("Found privat token. Getting transactions...");
      return StreamEx.of(config.accounts())
        .flatMap(account -> privatService.fetchTxesForAccount(account, privatToken.get(), startDate).stream())
        .toImmutableSet();
    }
  }

  public record PrivatTxFetcherConfig(String tokenName, List<String> accounts) { }
}
