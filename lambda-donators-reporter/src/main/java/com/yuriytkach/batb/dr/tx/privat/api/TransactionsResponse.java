package com.yuriytkach.batb.dr.tx.privat.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record TransactionsResponse(
  String status,
  @JsonProperty("exist_next_page") boolean existsNextPage,
  @JsonProperty("next_page_id") String nextPageId,
  List<Transaction> transactions
) {

}

