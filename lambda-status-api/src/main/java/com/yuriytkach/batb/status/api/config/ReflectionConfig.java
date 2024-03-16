package com.yuriytkach.batb.status.api.config;

import com.yuriytkach.batb.common.BankAccount;
import com.yuriytkach.batb.common.BankAccountStatus;
import com.yuriytkach.batb.common.FundInfo;
import com.yuriytkach.batb.common.Statistic;
import com.yuriytkach.batb.common.StatisticData;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {
  FundInfo.class,
  BankAccount.class,
  BankAccountStatus.class,
  Statistic.class,
  StatisticData.class,
  StatisticData.StatisticDataBuilder.class
})
public class ReflectionConfig {

}
