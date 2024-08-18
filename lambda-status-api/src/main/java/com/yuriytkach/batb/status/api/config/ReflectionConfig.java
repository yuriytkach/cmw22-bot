/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
