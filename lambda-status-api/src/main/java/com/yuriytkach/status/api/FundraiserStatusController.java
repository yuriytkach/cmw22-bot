package com.yuriytkach.status.api;

import java.time.Instant;

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

  @GET
  @Path("/{fundraiserId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    @PathParam("fundraiserId") final String fundraiserId
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Get Fundraiser Status: {} AWS Request ID: {}", fundraiserId, context.getAwsRequestId());

    if (!fundraiserId.equals("ppo")) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    final var fundraiserStatus = FundraiserStatus.builder()
      .goal(1500000)
      .raised(150)
      .spent(0)
      .name("Приціли для моб ППО")
      .description("""
        30% БПЛА і ракет йдуть транзитом через Чернігівську область на Київ. Що робити? Збивати, нах..! \
        Долучайтеся до збору на тепловізійні приціли і кріплення для мобільних груп ППО на пікапах, \
        які не пропустять летючу нечисть на наші мирні міста!\
        """)
      .lastUpdatedAt(Instant.parse("2024-01-31T20:15:17Z"))
      .build();

    return addCorsHeaders(Response.ok(fundraiserStatus))
      .cacheControl(createCacheControl())
      .build();
  }

  private CacheControl createCacheControl() {
    final CacheControl cacheControl = new CacheControl();
    cacheControl.setMaxAge(60);
    return cacheControl;
  }

  private Response.ResponseBuilder addCorsHeaders(final Response.ResponseBuilder responseBuilder) {
    return responseBuilder
      .header("Access-Control-Allow-Headers", "*")
      .header("Access-Control-Allow-Methods", "GET,HEAD,OPTIONS")
      .header("Access-Control-Allow-Origin", "*");
  }

}
