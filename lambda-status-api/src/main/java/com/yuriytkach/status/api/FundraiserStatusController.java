package com.yuriytkach.status.api;

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

  @GET
  @Path("/{fundraiserId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    @PathParam("fundraiserId") final String fundraiserId
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Get Fundraiser Status: {} AWS Request ID: {}", fundraiserId, context.getAwsRequestId());

    final var fundInfo = fundService.getFundStatus(fundraiserId);

    return fundInfo.map(fund -> {
      log.info("Fundraiser status: {}", fund);

      final var fundraiserStatus = FundraiserStatusResponse.builder()
        .goal(fund.info().goal())
        .raised(fund.raised() / 100)
        .spent(fund.spent() / 100)
        .name(fund.info().name())
        .description(fund.info().description())
        .lastUpdatedAt(fund.updatedAt())
        .build();

      return addCorsHeaders(Response.ok(fundraiserStatus))
        .cacheControl(createCacheControl())
        .build();
    }).orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
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
