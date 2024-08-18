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

package com.yuriytkach.batb.status.api;

import org.slf4j.MDC;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/status")
@RequiredArgsConstructor
public class FundraiserStatusController {

  private final FundService fundService;
  private final CacheControlProperties cacheControlProperties;

  @GET
  @Path("/{fundraiserId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    @PathParam("fundraiserId") final String fundraiserId
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Get Fundraiser Status: {} AWS Request ID: {}", fundraiserId, context.getAwsRequestId());

    final var fundStatus = fundService.getFundStatus(fundraiserId);

    return fundStatus.map(fund -> {
      log.info("Fundraiser status: {}", fund);
      return Response.ok(fund).cacheControl(createCacheControl()).build();
    }).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
  }

  private CacheControl createCacheControl() {
    final CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge((int) cacheControlProperties.maxAge().fundStatus().getSeconds());
    return cacheControl;
  }

}
