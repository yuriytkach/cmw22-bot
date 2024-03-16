package com.yuriytkach.batb.status.api;

import org.slf4j.MDC;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

  private final StatsService statsService;
  private final CacheControlProperties cacheControlProperties;

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response stats(
    @Context com.amazonaws.services.lambda.runtime.Context context
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Get Stats. AWS Request ID: {}", context.getAwsRequestId());

    final var result = statsService.getStatistic();

    return result.map(stats -> {
      log.info("Statistics: {}", stats);
      return Response.ok(stats).cacheControl(createCacheControl()).build();
    }).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
  }

  private CacheControl createCacheControl() {
    final CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge((int) cacheControlProperties.maxAge().stats().getSeconds());
    return cacheControl;
  }

}
