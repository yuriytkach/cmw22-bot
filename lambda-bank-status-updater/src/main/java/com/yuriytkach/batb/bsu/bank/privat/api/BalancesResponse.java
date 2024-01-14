package com.yuriytkach.batb.bsu.bank.privat.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record BalancesResponse(
  String status,
  @JsonProperty("exist_next_page") boolean existsNextPage,
  @JsonProperty("next_page_id") String nextPageId,
  List<Balance> balances
) {

}
