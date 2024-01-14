package com.yuriytkach.batb.bsu.bank.privat.api;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Balance (
  String acc,
  String currency,
  String balanceOut,
  String balanceOutEq,

  String dpd
) { }
