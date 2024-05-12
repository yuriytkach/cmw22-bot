package com.yuriytkach.batb.dr;

import java.time.Instant;

public record Donator(String name, Long amount, int count, Instant lastTxDateTime) { }
