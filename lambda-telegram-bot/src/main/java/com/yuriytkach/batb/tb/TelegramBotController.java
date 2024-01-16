package com.yuriytkach.batb.tb;

import org.slf4j.MDC;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/hook")
@RequiredArgsConstructor
public class TelegramBotController {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/event")
  public Response hook(
    @Context com.amazonaws.services.lambda.runtime.Context context,
    final String body
  ) {
    MDC.put("awsRequestId", context.getAwsRequestId());
    log.info("Telegram Book Hook. AWS Request ID: {}", context.getAwsRequestId());
    log.info("Body: {}", body);
    return Response.ok(new Resp("world", "yes")).build();
  }

  public record Resp(String hello, String world) { }

}
