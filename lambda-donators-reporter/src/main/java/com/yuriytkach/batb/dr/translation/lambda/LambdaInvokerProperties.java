package com.yuriytkach.batb.dr.translation.lambda;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "translation.lambda")
interface LambdaInvokerProperties {

  String functionName();

}
