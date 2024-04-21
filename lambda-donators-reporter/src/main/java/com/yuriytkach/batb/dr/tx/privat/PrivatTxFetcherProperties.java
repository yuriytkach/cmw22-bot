package com.yuriytkach.batb.dr.tx.privat;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "txfetcher.privat")
public interface PrivatTxFetcherProperties {

  String configKey();

}
