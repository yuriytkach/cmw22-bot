package com.yuriytkach.batb.bsu.bank.mono;

import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.yuriytkach.batb.bsu.bank.mono.api.MonoApi;
import com.yuriytkach.batb.bsu.bank.mono.api.MonoJar;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class MonoService {

  @RestClient
  private final MonoApi monoApi;

  public Optional<MonoJar> readJarStatus(final String id) {
    try {
      log.debug("Reading jar status for id {}", id);
      return Optional.ofNullable(monoApi.jarStatus(id));
    } catch (final Exception ex) {
      log.error("Error while reading jar status for id {}: {}", id, ex.getMessage());
      return Optional.empty();
    }
  }
}
