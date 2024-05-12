package com.yuriytkach.batb.dr.tx.mono.token;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "txfetcher.mono-token")
public interface MonoTokenTxFetcherProperties {

  String configKey();

}
