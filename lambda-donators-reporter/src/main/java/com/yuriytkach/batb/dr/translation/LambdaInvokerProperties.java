package com.yuriytkach.batb.dr.translation;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "translation.lambda")
interface LambdaInvokerProperties {

  String functionName();

}
