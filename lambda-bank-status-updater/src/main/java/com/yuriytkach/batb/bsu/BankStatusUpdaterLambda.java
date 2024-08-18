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

package com.yuriytkach.batb.bsu;

import org.slf4j.MDC;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.yuriytkach.batb.bsu.stats.StatsUpdater;
import com.yuriytkach.batb.common.StatusUpdateRequest;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named("bankStatusUpdater")
public class BankStatusUpdaterLambda implements RequestHandler<StatusUpdateRequest, Void> {

  @Inject
  CurrentFundraiserUpdater currentFundraiserUpdater;

  @Inject
  AllAccountsUpdater allAccountsUpdater;

  @Inject
  StatsUpdater statsUpdater;

  @Override
  @SneakyThrows
  public Void handleRequest(final StatusUpdateRequest input, final Context context) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Bank Status Updater for Input {}. AWS Request ID: {}", input, context.getAwsRequestId());

    switch (input.check()) {
      case CURRENT -> currentFundraiserUpdater.update();
      case FULL -> allAccountsUpdater.update();
      case STATS -> statsUpdater.update();
    }

    return null;
  }
}
