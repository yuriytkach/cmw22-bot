package com.yuriytkach.batb.status.api.config;

import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.FundInfo;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
  FundInfo.class,
  BankAccount.class,
  BankAccountStatus.class
})
public class ReflectionConfig {

}
